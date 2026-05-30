package com.pinkpetal.periodtracker.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "reminders")
public class Reminder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "user_id", nullable = false, length = 50)
    private String userId;

    @Column(nullable = false, length = 50)
    private String type;

    @Column(nullable = false, length = 50)
    private String time;

    @Column(length = 20)
    private String status = "Active";

    @Column(length = 255)
    private String notes;

    @Column(name = "repeat_schedule", length = 50)
    private String repeatSchedule = "Once";

    @Column(name = "sound_enabled")
    private Boolean soundEnabled = true;

    @Column(name = "vibration_enabled")
    private Boolean vibrationEnabled = true;

    @Column(name = "interval_minutes")
    private Integer intervalMinutes = 0;

    @Column(name = "snoozed")
    private Boolean snoozed = false;

    public Reminder() {}

    public Reminder(String userId, String type, String time, String status) {
        this.userId = userId;
        this.type = type;
        this.time = time;
        this.status = status;
    }

    public Reminder(String userId, String type, String time, String status, String notes, String repeatSchedule, boolean soundEnabled, boolean vibrationEnabled, Integer intervalMinutes, boolean snoozed) {
        this.userId = userId;
        this.type = type;
        this.time = time;
        this.status = status;
        this.notes = notes;
        this.repeatSchedule = repeatSchedule;
        this.soundEnabled = soundEnabled;
        this.vibrationEnabled = vibrationEnabled;
        this.intervalMinutes = intervalMinutes;
        this.snoozed = snoozed;
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getRepeatSchedule() {
        return repeatSchedule;
    }

    public void setRepeatSchedule(String repeatSchedule) {
        this.repeatSchedule = repeatSchedule;
    }

    public boolean isSoundEnabled() {
        return soundEnabled != null ? soundEnabled : true;
    }

    public void setSoundEnabled(boolean soundEnabled) {
        this.soundEnabled = soundEnabled;
    }

    public boolean isVibrationEnabled() {
        return vibrationEnabled != null ? vibrationEnabled : true;
    }

    public void setVibrationEnabled(boolean vibrationEnabled) {
        this.vibrationEnabled = vibrationEnabled;
    }

    public Integer getIntervalMinutes() {
        return intervalMinutes;
    }

    public void setIntervalMinutes(Integer intervalMinutes) {
        this.intervalMinutes = intervalMinutes;
    }

    public boolean isSnoozed() {
        return snoozed != null ? snoozed : false;
    }

    public void setSnoozed(boolean snoozed) {
        this.snoozed = snoozed;
    }
}
