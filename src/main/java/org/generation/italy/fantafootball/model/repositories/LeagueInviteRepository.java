package org.generation.italy.fantafootball.model.repositories;

import org.generation.italy.fantafootball.model.entities.LeagueInvite;
import org.generation.italy.fantafootball.model.entities.LeagueInviteStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LeagueInviteRepository extends JpaRepository<LeagueInvite, Long> {
    List<LeagueInvite> findAllByInvitedUserIdAndStatus(Long invitedUserId, LeagueInviteStatus status);
    boolean existsByLeagueIdAndInvitedUserIdAndStatus(Long leagueId, Long invitedUserId, LeagueInviteStatus status);
}