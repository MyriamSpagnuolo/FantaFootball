package org.generation.italy.fantafootball.model.repositories;

import org.generation.italy.fantafootball.model.entities.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PlayerRepository extends JpaRepository<Player, Long>, JpaSpecificationExecutor<Player> {
    @Query("""
            select p from Player p
            where not exists (
                select tp.id from TeamPlayer tp
                where tp.player = p and tp.league.id = :leagueId
                  and tp.transferDate is null
            )
            order by p.id
            """)
    List<Player> findAvailableByLeagueId(@Param("leagueId") Long leagueId);

    Optional<Player> findByNameAndSurnameAndRealTeamNameAndRealTeamShirtNum(
            String name, String surname, String realTeamName, int realTeamShirtNum);

    Optional<Player> findByExternalId(Long externalId);
}
