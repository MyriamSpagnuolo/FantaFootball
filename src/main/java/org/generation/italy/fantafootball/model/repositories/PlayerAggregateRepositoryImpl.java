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
    public PriceRangeResponse findPriceRange(PlayerFilterRequest filterRequest) {
        var criteriaBuilder = entityManager.getCriteriaBuilder();
        var query = criteriaBuilder.createQuery(Object[].class);
        var root = query.from(Player.class);
        var predicates = new ArrayList<Predicate>();
        if (filterRequest.role() != null && !filterRequest.role().isEmpty()) predicates
                .add(root.get("role").in(filterRequest.role()));
        if (filterRequest.realTeamName() != null && !filterRequest.realTeamName()
                .isEmpty()) predicates.add(criteriaBuilder.lower(root.get("realTeamName"))
                .in(filterRequest.realTeamName().stream().map(s -> s.trim().toLowerCase(Locale.ROOT)).toList()));
        if (filterRequest.injured() != null) predicates.add(criteriaBuilder
                .equal(root.get("injured"), filterRequest.injured()));
        if (filterRequest.search() != null && !filterRequest.search().isBlank()) {
            String e = filterRequest.search().trim().toLowerCase(Locale.ROOT).
                    replace("!", "!!")
                    .replace("%", "!%")
                    .replace("_", "!_");
            String p = "%" + e + "%";
            predicates.add(criteriaBuilder.or(
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), p, '!'),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("surname")), p, '!'),
                    criteriaBuilder.like(criteriaBuilder.lower(criteriaBuilder.concat(
                            criteriaBuilder.concat(root.get("name"), " "), root.get("surname"))), p, '!')));
        }
        query.multiselect(criteriaBuilder.min(root.get("price")),
                            criteriaBuilder.max(root.get("price"))).where(predicates.toArray(Predicate[]::new));
        Object[] row = entityManager.createQuery(query).getSingleResult();
        return new PriceRangeResponse((Integer) row[0], (Integer) row[1]);
    }
}
