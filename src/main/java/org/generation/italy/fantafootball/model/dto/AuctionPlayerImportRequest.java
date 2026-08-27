package org.generation.italy.fantafootball.model.dto;

public record AuctionPlayerImportRequest(
        String name,
        String surname,
        String realTeamName,
        int realTeamShirtNum,
        int purchasePrice
) { }
