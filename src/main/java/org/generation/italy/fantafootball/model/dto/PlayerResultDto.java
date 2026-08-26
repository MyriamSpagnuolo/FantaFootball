package org.generation.italy.fantafootball.model.dto;

import java.math.BigDecimal;

public record PlayerResultDto (
        String name,
        String surname,
        String realTeamName,
        int realTeamShirtNum,
        BigDecimal rating,
        int goalNum,
        int goalConceded,
        int autogoalNum,
        int assistNum,
        int penaltySaved,
        int penaltyFailed,
        Boolean cleanSheet,
        int yellowCard,
        boolean redCard
) {}
