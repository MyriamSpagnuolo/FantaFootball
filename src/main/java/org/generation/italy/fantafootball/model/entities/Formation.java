package org.generation.italy.fantafootball.model.entities;

import java.util.Arrays;
import java.util.Optional;

/**
 * I 7 moduli di formazione standard supportati (vedi seed-lineup-type.sql).
 * Insieme chiuso per costruzione: a differenza della tabella lineup_type,
 * qui non è possibile "creare" a runtime una combinazione non prevista.
 */
public enum Formation {
    // Stessi id già usati in database/seed-lineup-type.sql: mantenuti qui
    // per poter seedare/riconoscere le righe di lineup_type in modo stabile.
    THREE_FOUR_THREE(1L, 3, 4, 3),
    THREE_FIVE_TWO(2L, 3, 5, 2),
    FOUR_THREE_THREE(3L, 4, 3, 3),
    FOUR_FOUR_TWO(4L, 4, 4, 2),
    FOUR_FIVE_ONE(5L, 4, 5, 1),
    FIVE_THREE_TWO(6L, 5, 3, 2),
    FIVE_FOUR_ONE(7L, 5, 4, 1);

    private final Long id;
    private final int defenderNum;
    private final int midfielderNum;
    private final int forwardNum;

    Formation(Long id, int defenderNum, int midfielderNum, int forwardNum) {
        this.id = id;
        this.defenderNum = defenderNum;
        this.midfielderNum = midfielderNum;
        this.forwardNum = forwardNum;
    }

    public Long getId() {
        return id;
    }

    public int getDefenderNum() {
        return defenderNum;
    }

    public int getMidfielderNum() {
        return midfielderNum;
    }

    public int getForwardNum() {
        return forwardNum;
    }

    public static Optional<Formation> fromCounts(int defenderNum, int midfielderNum, int forwardNum) {
        return Arrays.stream(values())
                .filter(f -> f.defenderNum == defenderNum
                        && f.midfielderNum == midfielderNum
                        && f.forwardNum == forwardNum)
                .findFirst();
    }
}
