package org.generation.italy.fantafootball.model.repositories;

import org.generation.italy.fantafootball.model.dto.PlayerFilterRequest;
import org.generation.italy.fantafootball.model.dto.PriceRangeResponse;

public interface PlayerAggregateRepository {
    PriceRangeResponse findPriceRange(PlayerFilterRequest filters);
}
