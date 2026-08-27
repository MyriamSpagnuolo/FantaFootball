package org.generation.italy.fantafootball.model.dto;

import java.math.BigDecimal;

// Dentro PlayerResultDto non mettiamo id, perché l'id di PlayerResult viene generato quando salviamo la entity.
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
