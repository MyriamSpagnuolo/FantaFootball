package org.generation.italy.fantafootball.model.repositories;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.Predicate;
import org.generation.italy.fantafootball.model.dto.PlayerFilterRequest;
import org.generation.italy.fantafootball.model.dto.PriceRangeResponse;
import org.generation.italy.fantafootball.model.entities.Player;
import java.util.ArrayList;
import java.util.Locale;

public class PlayerAggregateRepositoryImpl implements PlayerAggregateRepository {
    @PersistenceContext private EntityManager entityManager;

    @Override
    public PriceRangeResponse findPriceRange(PlayerFilterRequest f) {
        var cb = entityManager.getCriteriaBuilder();
        var q = cb.createQuery(Object[].class);
        var r = q.from(Player.class);
        var predicates = new ArrayList<Predicate>();
        if (f.role() != null && !f.role().isEmpty()) predicates.add(r.get("role").in(f.role()));
        if (f.realTeamName() != null && !f.realTeamName().isEmpty()) predicates.add(cb.lower(r.get("realTeamName")).in(f.realTeamName().stream().map(s -> s.trim().toLowerCase(Locale.ROOT)).toList()));
        if (f.injured() != null) predicates.add(cb.equal(r.get("injured"), f.injured()));
        if (f.search() != null && !f.search().isBlank()) {
            String e = f.search().trim().toLowerCase(Locale.ROOT).replace("!", "!!").replace("%", "!%").replace("_", "!_");
            String p = "%" + e + "%";
            predicates.add(cb.or(cb.like(cb.lower(r.get("name")), p, '!'), cb.like(cb.lower(r.get("surname")), p, '!'), cb.like(cb.lower(cb.concat(cb.concat(r.get("name"), " "), r.get("surname"))), p, '!')));
        }
        q.multiselect(cb.min(r.get("price")), cb.max(r.get("price"))).where(predicates.toArray(Predicate[]::new));
        Object[] row = entityManager.createQuery(q).getSingleResult();
        return new PriceRangeResponse((Integer) row[0], (Integer) row[1]);
    }
}
