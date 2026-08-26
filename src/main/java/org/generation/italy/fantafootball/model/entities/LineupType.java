package org.generation.italy.fantafootball.model.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "lineup_type")
public class LineupType {

    // No identity/sequence on this column in the DB: ids are assigned explicitly
    // when seeding the fixed set of lineup types.
    @Id
    @Column(name = "id")
    private Long id;

    @Column(name = "defender_num", nullable = false)
    private int defenderNum;

    @Column(name = "midfielder_num", nullable = false)
    private int midfielderNum;

    // DB column is misspelled "foward_num"; kept as-is to match the actual schema.
    @Column(name = "foward_num", nullable = false)
    private int forwardNum;

    public LineupType() {
    }

    public LineupType(Long id, int defenderNum, int midfielderNum, int forwardNum) {
        this.id = id;
        this.defenderNum = defenderNum;
        this.midfielderNum = midfielderNum;
        this.forwardNum = forwardNum;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getDefenderNum() {
        return defenderNum;
    }

    public void setDefenderNum(int defenderNum) {
        this.defenderNum = defenderNum;
    }

    public int getMidfielderNum() {
        return midfielderNum;
    }

    public void setMidfielderNum(int midfielderNum) {
        this.midfielderNum = midfielderNum;
    }

    public int getForwardNum() {
        return forwardNum;
    }

    public void setForwardNum(int forwardNum) {
        this.forwardNum = forwardNum;
    }
}
