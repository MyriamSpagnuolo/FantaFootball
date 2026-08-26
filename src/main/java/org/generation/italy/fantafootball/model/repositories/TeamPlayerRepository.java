package org.generation.italy.fantafootball.model.repositories;

import org.generation.italy.fantafootball.model.entities.Team;
import org.generation.italy.fantafootball.model.entities.TeamPlayer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TeamPlayerRepository extends JpaRepository<TeamPlayer,Long> {
    // 1. Tutti i giocatori di una squadra
    List<TeamPlayer> findAllByTeamId(Long teamId);

    // 2. Verifica esistenza per il vincolo UNIQUE (name, surname, real_team_name, real_team_shirt_num)
    boolean existsByNameAndSurnameAndRealTeamNameAndRealTeamShirtNum(String name, String surname, String realTeamName,
                                                                     Integer realTeamShirtNum);
}

