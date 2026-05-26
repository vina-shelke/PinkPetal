package com.pinkpetal.periodtracker.models;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "mood_logs")
public class MoodLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "user_id", nullable = false, length = 50)
    private String userId;

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false, length = 50)
    private String mood;

    @Column(length = 255)
    private String notes;

    public MoodLog() {}

    public MoodLog(String userId, LocalDate date, String mood, String notes) {
        this.userId = userId;
        this.date = date;
        this.mood = mood;
        this.notes = notes;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getMood() {
        return mood;
    }

    public void setMood(String mood) {
        this.mood = mood;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
