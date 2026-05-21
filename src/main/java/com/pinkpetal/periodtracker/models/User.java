package com.pinkpetal.periodtracker.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;

@Entity
@Table(name = "users")
public class User {

    @Id
    @Column(name = "user_id", length = 50)
    private String userId;

    @Column(name = "name", length = 100)
    private String name;

    @Column(nullable = false)
    private String password;

    @Column(name = "cycle_length")
    private Integer cycleLength = 28;

    @Column(name = "last_period_date")
    private LocalDate lastPeriodDate;

    @Column(name = "registration_date")
    private LocalDate registrationDate = LocalDate.now();

    public User() {}

    public User(String userId, String name, String password, Integer cycleLength, LocalDate lastPeriodDate) {
        this.userId = userId;
        this.name = name;
        this.password = password;
        this.cycleLength = cycleLength;
        this.lastPeriodDate = lastPeriodDate;
        this.registrationDate = LocalDate.now();
    }

    public User(String userId, String name, String password, Integer cycleLength, LocalDate lastPeriodDate, LocalDate registrationDate) {
        this.userId = userId;
        this.name = name;
        this.password = password;
        this.cycleLength = cycleLength;
        this.lastPeriodDate = lastPeriodDate;
        this.registrationDate = registrationDate;
    }

    public User(String userId, String password, Integer cycleLength, LocalDate lastPeriodDate) {
        this.userId = userId;
        this.name = userId;
        this.password = password;
        this.cycleLength = cycleLength;
        this.lastPeriodDate = lastPeriodDate;
        this.registrationDate = LocalDate.now();
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Integer getCycleLength() {
        return cycleLength;
    }

    public void setCycleLength(Integer cycleLength) {
        this.cycleLength = cycleLength;
    }

    public LocalDate getLastPeriodDate() {
        return lastPeriodDate;
    }

    public void setLastPeriodDate(LocalDate lastPeriodDate) {
        this.lastPeriodDate = lastPeriodDate;
    }

    public LocalDate getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(LocalDate registrationDate) {
        this.registrationDate = registrationDate;
    }
}
