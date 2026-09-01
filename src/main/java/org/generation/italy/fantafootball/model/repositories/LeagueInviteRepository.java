package org.generation.italy.fantafootball.model.repositories;

import org.generation.italy.fantafootball.model.entities.LeagueInvite;
import org.generation.italy.fantafootball.model.entities.LeagueInviteStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LeagueInviteRepository extends JpaRepository<LeagueInvite, Long> {
    List<LeagueInvite> findAllByInvitedUserIdAndStatus(Long invitedUserId, LeagueInviteStatus status);
    boolean existsByLeagueIdAndInvitedUserIdAndStatus(Long leagueId, Long invitedUserId, LeagueInviteStatus status);
    Optional<LeagueInvite> findTopByLeagueIdAndInvitedUserIdOrderBySentDateDesc(Long leagueId, Long invitedUserId);
}
