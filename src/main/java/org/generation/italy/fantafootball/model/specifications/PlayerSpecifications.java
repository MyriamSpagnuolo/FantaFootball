package org.generation.italy.fantafootball.model.specifications;

import org.generation.italy.fantafootball.model.entities.Player;
import org.generation.italy.fantafootball.model.entities.PlayerRole;
import org.springframework.data.jpa.domain.Specification;

public final class PlayerSpecifications {

    private PlayerSpecifications() {
    }

    public static Specification<Player> hasRole(PlayerRole role) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("role"), role);
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
