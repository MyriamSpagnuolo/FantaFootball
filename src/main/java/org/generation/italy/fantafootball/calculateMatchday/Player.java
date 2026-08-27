package org.generation.italy.fantafootball.calculateMatchday;

public class Player {
    private Long id;
    private String name;
    private String surname;
    private int shirtNumber;
    private double matchdayRating;
    private Role role;
    private double vote;

    public double getVote() {
        return vote;
    }

    public Role getRole(){
        return role;
    }
}
