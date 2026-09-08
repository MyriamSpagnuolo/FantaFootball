package org.generation.italy.fantafootball.controllers;

import org.generation.italy.fantafootball.model.dto.PlayerResponse;
import org.generation.italy.fantafootball.model.entities.*;
import org.generation.italy.fantafootball.model.repositories.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AvailablePlayersIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired AppUserRepository users;
    @Autowired LeagueRepository leagues;
    @Autowired TeamRepository teams;
    @Autowired PlayerRepository players;
    @Autowired TeamPlayerRepository roster;

    private AppUser admin;
    private AppUser member;
    private League league;
    private Team team;
    private Player first;
    private Player second;

    @BeforeEach
    void setUp() {
        admin = user("admin");
        member = user("member");
        league = league("MAIN");
        team = team(member, league);
        first = players.save(new Player(101L, "Mario", "Rossi", "Roma", 9, 20, false, PlayerRole.A));
        second = players.save(new Player(102L, "Luca", "Verdi", "Milan", 8, 15, true, PlayerRole.C));
    }

    @Test
    void emptyLeagueReturnsEntireCatalogWithPlayerResponseFields() throws Exception {
        PlayerResponse expected = PlayerResponse.fromEntity(first);
        mvc.perform(get(url()).with(jwt().jwt(j -> j.subject(member.getUsername()).claim("tokenVersion", 0).claim("uid", member.getId()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(expected.id()))
                .andExpect(jsonPath("$[0].externalId").value(expected.externalId()))
                .andExpect(jsonPath("$[0].name").value(expected.name()))
                .andExpect(jsonPath("$[0].surname").value(expected.surname()))
                .andExpect(jsonPath("$[0].role").value(expected.role().name()))
                .andExpect(jsonPath("$[0].realTeamName").value(expected.realTeamName()))
                .andExpect(jsonPath("$[0].realTeamShirtNum").value(expected.realTeamShirtNum()))
                .andExpect(jsonPath("$[0].price").value(expected.price()))
                .andExpect(jsonPath("$[0].injured").value(expected.injured()));
    }

    @Test
    void excludesPlayersOwnedByAnyTeamInLeague() throws Exception {
        Team otherTeam = team(user("other"), league);
        own(otherTeam, first);
        mvc.perform(get(url()).with(jwt().jwt(j -> j.subject(member.getUsername()).claim("tokenVersion", 0).claim("uid", member.getId()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(second.getId()));
    }

    @Test
    void missingLeagueReturns404BeforeCheckingMembership() throws Exception {
        mvc.perform(get("/api/leagues/999999/players/available")
                        .with(jwt().jwt(j -> j.subject(member.getUsername()).claim("tokenVersion", 0).claim("uid", member.getId()))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("LEAGUE_NOT_FOUND"));
    }

    @Test
    void nonMemberReturns403() throws Exception {
        AppUser outsider = user("outsider");
        mvc.perform(get(url()).with(jwt().jwt(j -> j.subject(outsider.getUsername()).claim("tokenVersion", 0).claim("uid", outsider.getId()))))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminWithoutTeamHasAccess() throws Exception {
        mvc.perform(get(url()).with(jwt().jwt(j -> j.subject(admin.getUsername()).claim("tokenVersion", 0).claim("uid", admin.getId()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void successfulPurchaseRemovesPlayerFromAvailableCatalog() throws Exception {
        mvc.perform(get(url()).with(jwt().jwt(j -> j.subject(member.getUsername()).claim("tokenVersion", 0).claim("uid", member.getId()))))
                .andExpect(jsonPath("$.length()").value(2));
        mvc.perform(post("/api/leagues/{leagueId}/teams/{teamId}/players/{playerId}",
                        league.getId(), team.getId(), first.getId())
                        .with(jwt().jwt(j -> j.subject(admin.getUsername()).claim("tokenVersion", 0).claim("uid", admin.getId())))
                        .contentType("application/json").content("{\"purchasePrice\":10}"))
                .andExpect(status().is2xxSuccessful());
        mvc.perform(get(url()).with(jwt().jwt(j -> j.subject(member.getUsername()).claim("tokenVersion", 0).claim("uid", member.getId()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(second.getId()));
    }

    @Test
    void otherLeaguesAndHistoricalOwnershipDoNotExcludePlayers() throws Exception {
        own(team(member, league("OTHER")), first);
        TeamPlayer historical = own(team, second);
        historical.setTransferDate(LocalDate.now());
        roster.saveAndFlush(historical);
        mvc.perform(get(url()).with(jwt().jwt(j -> j.subject(member.getUsername()).claim("tokenVersion", 0).claim("uid", member.getId()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void allPlayersOwnedReturnsEmptyArray() throws Exception {
        own(team, first);
        own(team, second);
        mvc.perform(get(url()).with(jwt().jwt(j -> j.subject(member.getUsername()).claim("tokenVersion", 0).claim("uid", member.getId()))))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    private String url() {
        return "/api/leagues/" + league.getId() + "/players/available";
    }

    private AppUser user(String name) {
        AppUser user = new AppUser();
        user.setUsername(name);
        user.setEmail(name + "@test.local");
        user.setPasswordHash("test-hash");
        return users.save(user);
    }

    private League league(String code) {
        League value = new League(code, code, admin);
        value.setCreationDate(LocalDateTime.now());
        value.setBudget(500);
        return leagues.save(value);
    }

    private Team team(AppUser owner, League targetLeague) {
        Team value = new Team(owner.getUsername(), owner, targetLeague);
        value.setBudget(500);
        return teams.save(value);
    }

    private TeamPlayer own(Team owner, Player player) {
        return roster.saveAndFlush(new TeamPlayer(owner, owner.getLeague(), player, LocalDate.now(), 10));
    }
}
