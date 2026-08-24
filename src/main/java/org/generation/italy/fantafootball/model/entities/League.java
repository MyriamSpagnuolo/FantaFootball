package org.generation.italy.fantafootball.model.entities;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name="league")

public class League {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "league_id_seq_gen")
    @SequenceGenerator(
            name = "league_id_seq_gen",
            sequenceName = "seq_league_id",
            allocationSize = 1
    )
    @Column(name = "id")
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "invite_code")
    private String inviteCode;

    @ManyToOne
    @JoinColumn(name = "admin_user_id")
    private AppUser admin;

    @Column(name = "creation_date")
    private LocalDate creationDate;


}
