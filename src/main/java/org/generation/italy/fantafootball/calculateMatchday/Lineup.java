package org.generation.italy.fantafootball.calculateMatchday;

import java.util.List;

public class Lineup {

    private FormationType formation;
    private Player goalkeeper;
    private List<Player> defenders;
    private List<Player> midfielders;
    private List<Player> forwards;



    static Lineup insertMod(FormationType mod) {
        Lineup lineup = new Lineup();
        lineup.formation = mod;
        return lineup;

    }

    static Lineup insertPlayer(Lineup lineup, Player player){
        switch(player.getRole()) {
            case GK:
                if(lineup.goalkeeper==null)
                lineup.goalkeeper=player;
                break;
            case DEF:
                if(lineup.defenders.size()<lineup.formation.defenders)
                    lineup.defenders.add(player);
                break;
            case MID:
                if(lineup.midfielders.size()<lineup.formation.midfielders)
                    lineup.midfielders.add(player);
                break;
            case ST:
                if(lineup.forwards.size()<lineup.formation.forwards)
                    lineup.forwards.add(player);
                break;



        }
        return lineup ;
    }
}
