package org.generation.italy.fantafootball.model.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "player")
public class Player {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "player_id_seq_gen")
    @SequenceGenerator(
            name = "player_id_seq_gen",
            sequenceName = "seq_player_id",
            allocationSize = 1
    )
    @Column(name = "id")
    private Long id;

    @Column(name = "external_id", nullable = false, unique = true)
    private Long externalId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "surname", nullable = false)
    private String surname;

    @Column(name = "real_team_name", nullable = false)
    private String realTeamName;

    @Column(name = "real_team_shirt_num", nullable = false)
    private int realTeamShirtNum;

    @Column(name = "price", nullable = false)
    private int price;

    @Column(name = "is_injured", nullable = false)
    private boolean injured;

    @Enumerated(EnumType.STRING)
    @Column(name = "position", length = 1, nullable = false)
    private PlayerRole position;

    public Player() {
    }

    public Player(Long externalId, String name, String surname, String realTeamName, int realTeamShirtNum,
                  int price, boolean injured, PlayerRole position) {
        this.externalId = externalId;
        this.name = name;
        this.surname = surname;
        this.realTeamName = realTeamName;
        this.realTeamShirtNum = realTeamShirtNum;
        this.price = price;
        this.injured = injured;
        this.position = position;
    }

    public Long getId() {
        return id;
    }

    public Long getExternalId() {
        return externalId;
    }

    public void setExternalId(Long externalId) {
        this.externalId = externalId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getRealTeamName() {
        return realTeamName;
    }

    public void setRealTeamName(String realTeamName) {
        this.realTeamName = realTeamName;
    }

    public int getRealTeamShirtNum() {
        return realTeamShirtNum;
    }

    public void setRealTeamShirtNum(int realTeamShirtNum) {
        this.realTeamShirtNum = realTeamShirtNum;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public boolean isInjured() {
        return injured;
    }

    public void setInjured(boolean injured) {
        this.injured = injured;
    }

    public PlayerRole getPosition() {
        return position;
    }

    public void setPosition(PlayerRole position) {
        this.position = position;
    }
}
