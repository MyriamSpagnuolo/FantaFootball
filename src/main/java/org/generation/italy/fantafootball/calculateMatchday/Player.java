package org.generation.italy.fantafootball.calculateMatchday;

public class Player {
    private Long id;
    private String name;
    private String surname;
    private int shirtNumber;
    private double matchdayRating;
    private Role role;


    public Role getRole(){
        return role;
    }
}
