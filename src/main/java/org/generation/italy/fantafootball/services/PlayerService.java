package org.generation.italy.fantafootball.services;

import org.generation.italy.fantafootball.model.dto.PlayerFilterRequest;
import org.generation.italy.fantafootball.model.dto.PlayerResponse;
import org.generation.italy.fantafootball.model.dto.PageResponse;
import org.generation.italy.fantafootball.model.entities.Player;
import org.generation.italy.fantafootball.model.exceptions.BadRequestException;
import org.generation.italy.fantafootball.model.repositories.PlayerRepository;
import org.generation.italy.fantafootball.model.specifications.PlayerSpecifications;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
public class PlayerService {

    private final PlayerRepository playerRepository;

    public PlayerService(PlayerRepository playerRepository) {
        this.playerRepository = playerRepository;
    }

    @Transactional(readOnly = true)
    public PageResponse<PlayerResponse> findPlayers(PlayerFilterRequest filters, int page, int size) {
        validateFilters(filters);
        var pageable = PlayerPagination.of(page, size);

        Specification<Player> specification = Specification.allOf();

        if (filters.role() != null) {
            specification = specification.and(
                    PlayerSpecifications.hasRole(filters.role())
            );
        }

        if (filters.injured() != null) {
            specification = specification.and(
                    PlayerSpecifications.hasInjuryStatus(filters.injured())
            );
        }

        if (filters.minPrice() != null) {
            specification = specification.and(
                    PlayerSpecifications.priceGreaterThanOrEqualTo(filters.minPrice())
            );
        }

        if (filters.maxPrice() != null) {
            specification = specification.and(
                    PlayerSpecifications.priceLessThanOrEqualTo(filters.maxPrice())
            );
        }

        if (filters.realTeamName() != null) {
            specification = specification.and(
                    PlayerSpecifications.hasRealTeam(filters.realTeamName().trim())
            );
        }

        return PageResponse.fromPage(playerRepository.findAll(specification, pageable)
                .map(PlayerResponse::fromEntity));
    }

    private void validateFilters(PlayerFilterRequest filters) {
        if (filters == null) {
            throw new BadRequestException(
                    "INVALID_FILTERS",
                    "I filtri non possono essere null"
            );
        }

        if (filters.minPrice() != null && filters.minPrice() < 0) {
            throw new BadRequestException(
                    "INVALID_MIN_PRICE",
                    "Il prezzo minimo deve essere positivo"
            );
        }

        if (filters.maxPrice() != null && filters.maxPrice() < 0) {
            throw new BadRequestException(
                    "INVALID_MAX_PRICE",
                    "Il prezzo massimo deve essere positivo"
            );
        }

        if (filters.minPrice() != null
                && filters.maxPrice() != null
                && filters.minPrice() > filters.maxPrice()) {
            throw new BadRequestException(
                    "INVALID_PRICE_RANGE",
                    "Il prezzo minimo deve essere minore o uguale al prezzo massimo"
            );
        }

        if (filters.realTeamName() != null && filters.realTeamName().isBlank()) {
            throw new BadRequestException(
                    "INVALID_REAL_TEAM",
                    "La squadra reale deve essere valorizzata"
            );
        }
    }
}
