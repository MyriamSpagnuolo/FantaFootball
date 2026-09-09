package org.generation.italy.fantafootball.model.repositories;

import org.generation.italy.fantafootball.model.entities.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Optional;

public interface PlayerRepository extends JpaRepository<Player, Long>, JpaSpecificationExecutor<Player> {
    @Query("""
            select p from Player p
            where not exists (
                select tp.id from TeamPlayer tp
                where tp.player = p and tp.league.id = :leagueId
                  and tp.transferDate is null
            )
            and (:searchPattern is null
                or lower(p.name) like :searchPattern escape '!'
                or lower(p.surname) like :searchPattern escape '!'
                or lower(concat(concat(p.name, ' '), p.surname)) like :searchPattern escape '!')
            """)
    Page<Player> findAvailableByLeagueId(@Param("leagueId") Long leagueId,
                                       @Param("searchPattern") String searchPattern, Pageable pageable);

    Optional<Player> findByNameAndSurnameAndRealTeamNameAndRealTeamShirtNum(
            String name, String surname, String realTeamName, int realTeamShirtNum);

    Optional<Player> findByExternalId(Long externalId);
}
