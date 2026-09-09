package org.generation.italy.fantafootball.controllers;

import org.generation.italy.fantafootball.model.dto.PlayerResponse;
import org.generation.italy.fantafootball.model.entities.*;
import org.generation.italy.fantafootball.model.repositories.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
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
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].id").value(expected.id()))
                .andExpect(jsonPath("$.content[0].externalId").value(expected.externalId()))
                .andExpect(jsonPath("$.content[0].name").value(expected.name()))
                .andExpect(jsonPath("$.content[0].surname").value(expected.surname()))
                .andExpect(jsonPath("$.content[0].role").value(expected.role().name()))
                .andExpect(jsonPath("$.content[0].realTeamName").value(expected.realTeamName()))
                .andExpect(jsonPath("$.content[0].realTeamShirtNum").value(expected.realTeamShirtNum()))
                .andExpect(jsonPath("$.content[0].price").value(expected.price()))
                .andExpect(jsonPath("$.content[0].injured").value(expected.injured()));
    }

    @Test
    void excludesPlayersOwnedByAnyTeamInLeague() throws Exception {
        Team otherTeam = team(user("other"), league);
        own(otherTeam, first);
        mvc.perform(get(url()).with(jwt().jwt(j -> j.subject(member.getUsername()).claim("tokenVersion", 0).claim("uid", member.getId()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].id").value(second.getId()));
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
                .andExpect(jsonPath("$.content.length()").value(2));
    }

    @Test
    void successfulPurchaseRemovesPlayerFromAvailableCatalog() throws Exception {
        mvc.perform(get(url()).with(jwt().jwt(j -> j.subject(member.getUsername()).claim("tokenVersion", 0).claim("uid", member.getId()))))
                .andExpect(jsonPath("$.content.length()").value(2));
        mvc.perform(post("/api/leagues/{leagueId}/teams/{teamId}/players/{playerId}",
                        league.getId(), team.getId(), first.getId())
                        .with(jwt().jwt(j -> j.subject(admin.getUsername()).claim("tokenVersion", 0).claim("uid", admin.getId())))
                        .contentType("application/json").content("{\"purchasePrice\":10}"))
                .andExpect(status().is2xxSuccessful());
        mvc.perform(get(url()).with(jwt().jwt(j -> j.subject(member.getUsername()).claim("tokenVersion", 0).claim("uid", member.getId()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].id").value(second.getId()));
    }

    @Test
    void otherLeaguesAndHistoricalOwnershipDoNotExcludePlayers() throws Exception {
        own(team(member, league("OTHER")), first);
        TeamPlayer historical = own(team, second);
        historical.setTransferDate(LocalDate.now());
        roster.saveAndFlush(historical);
        mvc.perform(get(url()).with(jwt().jwt(j -> j.subject(member.getUsername()).claim("tokenVersion", 0).claim("uid", member.getId()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2));
    }

    @Test
    void allPlayersOwnedReturnsEmptyArray() throws Exception {
        own(team, first);
        own(team, second);
        mvc.perform(get(url()).with(jwt().jwt(j -> j.subject(member.getUsername()).claim("tokenVersion", 0).claim("uid", member.getId()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty()).andExpect(jsonPath("$.totalElements").value(0));
    }

    private String url() {
        return "/api/leagues/" + league.getId() + "/players/available";
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   ", "\t "})
    void blankSearchReturnsEntireCatalog(String search) throws Exception {
        search(search, 0, 20)
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.totalElements").value(2));
    }

    @ParameterizedTest
    @ValueSource(strings = {"ari", "oss", "mArIo rOsSi", "  MARIO ROSSI  ", "io Ros"})
    void searchesPartialNamesAndFullNameIgnoringCaseAndSurroundingSpaces(String search) throws Exception {
        search(search, 0, 20)
                .andExpect(jsonPath("$.content[0].id").value(first.getId()))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void searchFindsPlayerBeyondFirstUnfilteredPage() throws Exception {
        for (long id = 103; id < 128; id++) {
            players.save(new Player(id, "Nome", "Cognome", "Roma", 1, 10, false, PlayerRole.A));
        }
        Player target = players.save(new Player(128L, "Unico", "Bianchi", "Roma", 1, 10, false, PlayerRole.A));
        search("bian", 0, 20)
                .andExpect(jsonPath("$.content[0].id").value(target.getId()))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.hasNext").value(false));
    }

    @Test
    void searchCombinesLeagueAvailabilityAndFilteredPagination() throws Exception {
        Player third = players.save(new Player(103L, "Mario", "Rossi", "Roma", 1, 10, false, PlayerRole.A));
        Player fourth = players.save(new Player(104L, "Mario", "Rossi", "Roma", 2, 10, false, PlayerRole.A));
        own(team, first);
        own(team(member, league("OTHER")), third);
        for (int page = 0; page < 2; page++) {
            search("mario", page, 1)
                    .andExpect(jsonPath("$.content.length()").value(1))
                    .andExpect(jsonPath("$.content[0].id").value(page == 0 ? third.getId() : fourth.getId()))
                    .andExpect(jsonPath("$.page").value(page))
                    .andExpect(jsonPath("$.size").value(1))
                    .andExpect(jsonPath("$.totalElements").value(2))
                    .andExpect(jsonPath("$.totalPages").value(2))
                    .andExpect(jsonPath("$.hasNext").value(page == 0));
        }
        search("mario", 2, 1)
                .andExpect(jsonPath("$.content").isEmpty())
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.hasNext").value(false));
    }

    @Test
    void noMatchReturnsZeroTotals() throws Exception {
        search("inesistente", 0, 20)
                .andExpect(jsonPath("$.content").isEmpty())
                .andExpect(jsonPath("$.totalElements").value(0))
                .andExpect(jsonPath("$.totalPages").value(0))
                .andExpect(jsonPath("$.hasNext").value(false));
    }

    @ParameterizedTest
    @ValueSource(strings = {"%", "_", "!", "\\", "!%_"})
    void likeSpecialCharactersAreLiteral(String literal) throws Exception {
        Player target = players.save(new Player(103L, "Special" + literal, "Test", "Roma", 1, 10, false, PlayerRole.A));
        search(literal, 0, 20)
                .andExpect(jsonPath("$.content[0].id").value(target.getId()))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void unauthenticatedSearchReturns401() throws Exception {
        mvc.perform(get(url()).param("search", "Mario"))
                .andExpect(status().isUnauthorized());
    }

    private org.springframework.test.web.servlet.ResultActions search(String search, int page, int size) throws Exception {
        return mvc.perform(get(url()).param("search", search)
                        .param("page", Integer.toString(page)).param("size", Integer.toString(size))
                        .with(jwt().jwt(j -> j.subject(member.getUsername()).claim("tokenVersion", 0).claim("uid", member.getId()))))
                .andExpect(status().isOk());
    }

    @Test
    void bothEndpointsReturnSeparatePagesAndTotals() throws Exception {
        for (String endpoint : new String[]{"/api/players", url()}) {
            mvc.perform(get(endpoint).param("page", "0").param("size", "1")
                            .with(jwt().jwt(j -> j.subject(member.getUsername()).claim("tokenVersion", 0).claim("uid", member.getId()))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content.length()").value(1))
                    .andExpect(jsonPath("$.content[0].id").value(first.getId()))
                    .andExpect(jsonPath("$.page").value(0))
                    .andExpect(jsonPath("$.size").value(1))
                    .andExpect(jsonPath("$.totalElements").value(2))
                    .andExpect(jsonPath("$.totalPages").value(2))
                    .andExpect(jsonPath("$.hasNext").value(true));
            mvc.perform(get(endpoint).param("page", "1").param("size", "1")
                            .with(jwt().jwt(j -> j.subject(member.getUsername()).claim("tokenVersion", 0).claim("uid", member.getId()))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content.length()").value(1))
                    .andExpect(jsonPath("$.content[0].id").value(second.getId()))
                    .andExpect(jsonPath("$.page").value(1))
                    .andExpect(jsonPath("$.totalElements").value(2))
                    .andExpect(jsonPath("$.hasNext").value(false));
        }
    }

    @Test
    void availableCountExcludesOwnedPlayersBeforePagination() throws Exception {
        own(team, first);
        mvc.perform(get(url()).param("size", "1")
                        .with(jwt().jwt(j -> j.subject(member.getUsername()).claim("tokenVersion", 0).claim("uid", member.getId()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(second.getId()))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.hasNext").value(false));
    }

    @Test
    void catalogFiltersApplyBeforePaginationAndCounting() throws Exception {
        players.save(new Player(103L, "Paolo", "Neri", "Roma", 10, 25, false, PlayerRole.A));
        mvc.perform(get("/api/players").param("role", "A").param("realTeamName", "Roma")
                        .param("minPrice", "20").param("maxPrice", "25").param("injured", "false")
                        .param("size", "1")
                        .with(jwt().jwt(j -> j.subject(member.getUsername()).claim("tokenVersion", 0).claim("uid", member.getId()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].id").value(first.getId()))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.totalPages").value(2));
    }

    @Test
    void defaultsLimitResultsToTwentyPlayers() throws Exception {
        for (long id = 103; id < 128; id++) {
            players.save(new Player(id, "Nome", "Cognome", "Roma", 1, 10, false, PlayerRole.A));
        }
        for (String endpoint : new String[]{"/api/players", url()}) {
            mvc.perform(get(endpoint)
                            .with(jwt().jwt(j -> j.subject(member.getUsername()).claim("tokenVersion", 0).claim("uid", member.getId()))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content.length()").value(20))
                    .andExpect(jsonPath("$.page").value(0))
                    .andExpect(jsonPath("$.size").value(20))
                    .andExpect(jsonPath("$.totalElements").value(27))
                    .andExpect(jsonPath("$.hasNext").value(true));
        }
    }

    @Test
    void pageBeyondLastReturnsEmptyContentWithTotals() throws Exception {
        for (String endpoint : new String[]{"/api/players", url()}) {
            mvc.perform(get(endpoint).param("page", "5").param("size", "1")
                            .with(jwt().jwt(j -> j.subject(member.getUsername()).claim("tokenVersion", 0).claim("uid", member.getId()))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isEmpty())
                    .andExpect(jsonPath("$.totalElements").value(2))
                    .andExpect(jsonPath("$.hasNext").value(false));
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"page=-1", "size=0", "size=-1", "size=101", "page=abc", "size=abc", "page=2147483647&size=100"})
    void invalidPaginationReturns400(String query) throws Exception {
        for (String endpoint : new String[]{"/api/players", url()}) {
            mvc.perform(get(endpoint + "?" + query)
                            .with(jwt().jwt(j -> j.subject(member.getUsername()).claim("tokenVersion", 0).claim("uid", member.getId()))))
                    .andExpect(status().isBadRequest());
        }
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
