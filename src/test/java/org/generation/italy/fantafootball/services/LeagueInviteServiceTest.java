package org.generation.italy.fantafootball.services;

import org.generation.italy.fantafootball.model.dto.CreateInviteRequest;
import org.generation.italy.fantafootball.model.entities.AppUser;
import org.generation.italy.fantafootball.model.entities.League;
import org.generation.italy.fantafootball.model.entities.LeagueInvite;
import org.generation.italy.fantafootball.model.entities.LeagueInviteStatus;
import org.generation.italy.fantafootball.model.exceptions.BadRequestException;
import org.generation.italy.fantafootball.model.exceptions.ConflictException;
import org.generation.italy.fantafootball.model.repositories.AppUserRepository;
import org.generation.italy.fantafootball.model.repositories.LeagueInviteRepository;
import org.generation.italy.fantafootball.model.repositories.LeagueRepository;
import org.generation.italy.fantafootball.model.repositories.TeamRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LeagueInviteServiceTest {

    @Mock
    LeagueInviteRepository leagueInviteRepository;
    @Mock
    LeagueRepository leagueRepository;
    @Mock
    AppUserRepository appUserRepository;
    @Mock
    TeamRepository teamRepository;
    @InjectMocks
    LeagueInviteService leagueInviteService;

    @Test
    void sendInviteRejectsSelfInvite() {
        AppUser admin = user(1L, "admin");
        League league = league(9L, admin);

        when(leagueRepository.findById(9L)).thenReturn(Optional.of(league));
        when(appUserRepository.findById(1L)).thenReturn(Optional.of(admin));
        when(appUserRepository.findByUsername("admin")).thenReturn(Optional.of(admin));

        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> leagueInviteService.sendInvite(9L, new CreateInviteRequest("admin"), 1L)
        );

        assertEquals("SAME_USER", exception.getErrorCode());
    }

    @Test
    void sendInviteCreatesPendingInviteWhenRequestIsValid() {
        AppUser admin = user(1L, "admin");
        AppUser invited = user(2L, "guest");
        League league = league(9L, admin);

        when(leagueRepository.findById(9L)).thenReturn(Optional.of(league));
        when(appUserRepository.findById(1L)).thenReturn(Optional.of(admin));
        when(appUserRepository.findByUsername("guest")).thenReturn(Optional.of(invited));
        when(leagueInviteRepository.existsByLeagueIdAndInvitedUserIdAndStatus(
                9L, 2L, LeagueInviteStatus.PENDING)).thenReturn(false);
        when(teamRepository.existsByUserIdAndLeagueId(2L, 9L)).thenReturn(false);
        when(leagueInviteRepository.save(any(LeagueInvite.class))).thenAnswer(invocation -> invocation.getArgument(0));

        leagueInviteService.sendInvite(9L, new CreateInviteRequest("guest"), 1L);

        ArgumentCaptor<LeagueInvite> captor = ArgumentCaptor.forClass(LeagueInvite.class);
        verify(leagueInviteRepository).save(captor.capture());
        assertEquals(LeagueInviteStatus.PENDING, captor.getValue().getStatus());
        assertEquals(invited, captor.getValue().getInvitedUser());
        assertNotNull(captor.getValue().getSentDate());
    }

    @Test
    void acceptInviteRejectsDifferentUser() {
        AppUser owner = user(2L, "guest");
        LeagueInvite invite = new LeagueInvite(league(9L, user(1L, "admin")), user(1L, "admin"), owner,
                LeagueInviteStatus.PENDING);

        when(leagueInviteRepository.findById(30L)).thenReturn(Optional.of(invite));

        ConflictException exception = assertThrows(
                ConflictException.class,
                () -> leagueInviteService.acceptInvite(30L, 99L)
        );

        assertEquals("NOT_YOUR_INVITE", exception.getErrorCode());
    }

    private static AppUser user(Long id, String username) {
        AppUser user = new AppUser();
        setId(user, id);
        user.setUsername(username);
        user.setEmail(username + "@test.local");
        return user;
    }

    private static League league(Long id, AppUser admin) {
        League league = new League("League", "CODE" + id, admin);
        setId(league, id);
        return league;
    }

    private static void setId(Object entity, Long id) {
        try {
            Field field = entity.getClass().getDeclaredField("id");
            field.setAccessible(true);
            field.set(entity, id);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }
}
