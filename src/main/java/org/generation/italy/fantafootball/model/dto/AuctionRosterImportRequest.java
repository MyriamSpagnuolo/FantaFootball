package org.generation.italy.fantafootball.model.dto;

import java.util.List;

public record AuctionRosterImportRequest(
        List<AuctionPlayerImportRequest> players
) { }
