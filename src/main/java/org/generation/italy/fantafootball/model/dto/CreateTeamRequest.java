package org.generation.italy.fantafootball.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class CreateTeamRequest {

    @NotBlank(message = "Il nome della squadra è obbligatorio")
    @Size(max = 100, message = "Il nome non può superare 100 caratteri")
    private String name;

    @NotNull(message = "L'utente proprietario è obbligatorio")
    private Long userId;

    @NotNull(message = "La lega è obbligatoria")
    private Long leagueId;

    public CreateTeamRequest() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getLeagueId() {
        return leagueId;
    }

    public void setLeagueId(Long leagueId) {
        this.leagueId = leagueId;
    }

}
