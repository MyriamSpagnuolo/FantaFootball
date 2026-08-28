package org.generation.italy.fantafootball.model.entities;

import jakarta.persistence.*;

import java.time.LocalDate;

// Giornata reale di campionato, condivisa da tutte le leghe: e' la fonte dei player_results.
// E' indipendente dal "round_number" di league_match, che invece e' la giornata relativa
// alla singola lega (parte da 1 quando la lega nasce, indipendentemente da a che punto sia
// il campionato reale in quel momento).
@Entity
@Table(name = "matchday")
public class Matchday {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "matchday_id_seq_gen")
    @SequenceGenerator(
            name = "matchday_id_seq_gen",
            sequenceName = "seq_matchday_id",
            allocationSize = 1
    )
    @Column(name = "id")
    private Long id;

    @Column(name = "number", nullable = false, unique = true)
    private int number;

    @Column(name = "date", nullable = false)
    private LocalDate date;

    @Column(name = "is_closed", nullable = false)
    private boolean closed = false;

    public Matchday() {
    }

    public Matchday(int number, LocalDate date) {
        this.number = number;
        this.date = date;
    }

    public Long getId() {
        return id;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public boolean isClosed() {
        return closed;
    }

    public void setClosed(boolean closed) {
        this.closed = closed;
    }
}
