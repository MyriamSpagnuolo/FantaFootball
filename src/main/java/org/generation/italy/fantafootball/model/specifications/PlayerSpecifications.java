package org.generation.italy.fantafootball.model.specifications;

import org.generation.italy.fantafootball.model.entities.Player;
import org.generation.italy.fantafootball.model.entities.PlayerRole;
import org.springframework.data.jpa.domain.Specification;
import java.util.List;
import java.util.Locale;

public final class PlayerSpecifications {

    private PlayerSpecifications() {
    }

    public static Specification<Player> hasRole(PlayerRole role) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("role"), role);
    }

    public static Specification<Player> hasAnyRole(List<PlayerRole> roles) {
        return (root, query, cb) -> root.get("role").in(roles);
    }

    public static Specification<Player> hasAnyRealTeam(List<String> teams) {
        return (root, query, cb) -> cb.lower(root.get("realTeamName")).in(
                teams.stream().map(s -> s.trim().toLowerCase(Locale.ROOT)).toList());
    }

    public static Specification<Player> nameContains(String value) {
        return (root, query, cb) -> {
            String escaped = value.toLowerCase(Locale.ROOT).replace("!", "!!").replace("%", "!%").replace("_", "!_");
            String pattern = "%" + escaped + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("name")), pattern, '!'),
                    cb.like(cb.lower(root.get("surname")), pattern, '!'),
                    cb.like(cb.lower(cb.concat(cb.concat(root.get("name"), " "), root.get("surname"))), pattern, '!'));
        };
    }

    public static Specification<Player> stableRoleOrdering() {
        return (root, query, cb) -> {
            if (query.getResultType() != Long.class && query.getResultType() != long.class) {
                var rank = cb.selectCase(root.get("role"))
                        .when(PlayerRole.P, 0).when(PlayerRole.D, 1).when(PlayerRole.C, 2).when(PlayerRole.A, 3)
                        .otherwise(4);
                query.orderBy(cb.asc(rank), cb.asc(root.get("surname")), cb.asc(root.get("name")), cb.asc(root.get("id")));
            }
            return cb.conjunction();
        };
    }

    public static Specification<Player> hasRealTeam(String realTeamName) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(
                        criteriaBuilder.lower(root.get("realTeamName")),
                        realTeamName.toLowerCase()
                );
    }

    public static Specification<Player> priceGreaterThanOrEqualTo(Integer minPrice) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.greaterThanOrEqualTo(
                        root.get("price"),
                        minPrice
                );
    }

    public static Specification<Player> priceLessThanOrEqualTo(Integer maxPrice) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.lessThanOrEqualTo(
                        root.get("price"),
                        maxPrice
                );
    }

    public static Specification<Player> hasInjuryStatus(Boolean injured) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(
                        root.get("injured"),
                        injured
                );
    }
}
