package org.generation.italy.fantafootball.services;

import org.generation.italy.fantafootball.model.dto.CreateInviteRequest;
import org.generation.italy.fantafootball.model.dto.InviteResponse;
import org.generation.italy.fantafootball.model.dto.RespondToInviteRequest;
import org.generation.italy.fantafootball.model.entities.AppUser;
import org.generation.italy.fantafootball.model.entities.League;
import org.generation.italy.fantafootball.model.entities.LeagueInvite;
import org.generation.italy.fantafootball.model.entities.LeagueInviteStatus;
import org.generation.italy.fantafootball.model.exceptions.BadRequestException;
import org.generation.italy.fantafootball.model.exceptions.ConflictException;
import org.generation.italy.fantafootball.model.exceptions.NotFoundException;
import org.generation.italy.fantafootball.model.repositories.AppUserRepository;
import org.generation.italy.fantafootball.model.repositories.LeagueInviteRepository;
import org.generation.italy.fantafootball.model.repositories.LeagueRepository;
import org.generation.italy.fantafootball.model.repositories.TeamRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class LeagueInviteService {

    private final LeagueInviteRepository leagueInviteRepository;
    private final LeagueRepository leagueRepository;
    private final AppUserRepository appUserRepository;
    private final TeamRepository teamRepository;

    public LeagueInviteService(LeagueInviteRepository leagueInviteRepository,
                               LeagueRepository leagueRepository,
                               AppUserRepository appUserRepository,
                               TeamRepository teamRepository) {
        this.leagueInviteRepository = leagueInviteRepository;
        this.leagueRepository = leagueRepository;
        this.appUserRepository = appUserRepository;
        this.teamRepository = teamRepository;
    }

    @Transactional
    public InviteResponse sendInvite(Long leagueId, CreateInviteRequest request) {
        if (request.invitedByUserId().equals(request.invitedUserId())) {
            throw new BadRequestException("SAME_USER", "Non puoi invitare te stesso");
        }

        Optional<League> existingLeague = leagueRepository.findById(leagueId);
        if (existingLeague.isEmpty()) {
            throw new NotFoundException("LEAGUE_NOT_FOUND", "Lega non trovata: " + leagueId);
        }
        League league = existingLeague.get();

        if (!league.getAdmin().getId().equals(request.invitedByUserId())) {
            throw new ConflictException("NOT_LEAGUE_ADMIN", "Solo l'admin della lega può inviare inviti");
        }

        Optional<AppUser> existingInvitedBy = appUserRepository.findById(request.invitedByUserId());
        if (existingInvitedBy.isEmpty()) {
            throw new NotFoundException("USER_NOT_FOUND", "Utente non trovato: " + request.invitedByUserId());
        }
        AppUser invitedBy = existingInvitedBy.get();

        Optional<AppUser> existingInvitedUser = appUserRepository.findById(request.invitedUserId());
        if (existingInvitedUser.isEmpty()) {
            throw new NotFoundException("USER_NOT_FOUND", "Utente non trovato: " + request.invitedUserId());
        }
        AppUser invitedUser = existingInvitedUser.get();

        boolean pendingExists = leagueInviteRepository.existsByLeagueIdAndInvitedUserIdAndStatus(
                leagueId, request.invitedUserId(), LeagueInviteStatus.PENDING);
        if (pendingExists) {
            throw new ConflictException("DUPLICATE_PENDING_INVITE",
                    "Esiste già un invito in sospeso per questo utente in questa lega");
        }

        boolean alreadyHasTeam = teamRepository.existsByUserIdAndLeagueId(request.invitedUserId(), leagueId);
        if (alreadyHasTeam) {
            throw new ConflictException("USER_ALREADY_HAS_TEAM",
                    "Questo utente ha già una squadra in questa lega");
        }

        LeagueInvite invite = new LeagueInvite(league, invitedBy, invitedUser, LeagueInviteStatus.PENDING);
        invite.setSentDate(LocalDateTime.now());

        LeagueInvite saved = leagueInviteRepository.save(invite);
        return InviteResponse.fromEntity(saved);
    }

    @Transactional
    public InviteResponse acceptInvite(Long inviteId, RespondToInviteRequest request) {
        Optional<LeagueInvite> existingInvite = leagueInviteRepository.findById(inviteId);
        if (existingInvite.isEmpty()) {
            throw new NotFoundException("INVITE_NOT_FOUND", "Invito non trovato: " + inviteId);
        }
        LeagueInvite invite = existingInvite.get();

        if (!invite.getInvitedUser().getId().equals(request.respondingUserId())) {
            throw new ConflictException("NOT_YOUR_INVITE", "Questo invito non appartiene a questo utente");
        }

        if (invite.getStatus() != LeagueInviteStatus.PENDING) {
            throw new ConflictException("INVITE_ALREADY_RESPONDED", "Hai già risposto a questo invito");
        }

        invite.setStatus(LeagueInviteStatus.ACCEPTED);
        invite.setResponseDate(LocalDateTime.now());

        LeagueInvite saved = leagueInviteRepository.save(invite);
        return InviteResponse.fromEntity(saved);
    }

    @Transactional
    public InviteResponse declineInvite(Long inviteId, RespondToInviteRequest request) {
        Optional<LeagueInvite> existingInvite = leagueInviteRepository.findById(inviteId);
        if (existingInvite.isEmpty()) {
            throw new NotFoundException("INVITE_NOT_FOUND", "Invito non trovato: " + inviteId);
        }
        LeagueInvite invite = existingInvite.get();

        if (!invite.getInvitedUser().getId().equals(request.respondingUserId())) {
            throw new ConflictException("NOT_YOUR_INVITE", "Questo invito non appartiene a questo utente");
        }

        if (invite.getStatus() != LeagueInviteStatus.PENDING) {
            throw new ConflictException("INVITE_ALREADY_RESPONDED", "Hai già risposto a questo invito");
        }

        invite.setStatus(LeagueInviteStatus.DECLINED);
        invite.setResponseDate(LocalDateTime.now());

        LeagueInvite saved = leagueInviteRepository.save(invite);
        return InviteResponse.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public List<InviteResponse> getPendingInvitesForUser(Long userId) {
        return leagueInviteRepository.findAllByInvitedUserIdAndStatus(userId, LeagueInviteStatus.PENDING).stream()
                .map(InviteResponse::fromEntity)
                .toList();
    }
}