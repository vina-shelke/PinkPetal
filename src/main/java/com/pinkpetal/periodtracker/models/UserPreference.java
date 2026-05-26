package com.pinkpetal.periodtracker.models;

import jakarta.persistence.*;

@Entity
@Table(name = "user_preferences")
public class UserPreference {

    @Id
    @Column(name = "user_id", length = 50)
    private String userId;

    @Column(name = "reminder_frequency", length = 50)
    private String reminderFrequency = "Balanced";

    @Column(name = "smart_notif_intensity", length = 50)
    private String smartNotifIntensity = "Balanced";

    @Column(name = "quiet_hours_start", length = 10)
    private String quietHoursStart = "22:00";

    @Column(name = "quiet_hours_end", length = 10)
    private String quietHoursEnd = "08:00";

    @Column(name = "sound_enabled")
    private boolean soundEnabled = true;

    @Column(name = "vibration_enabled")
    private boolean vibrationEnabled = true;

    @Column(name = "mood_support_enabled")
    private boolean moodSupportEnabled = true;

    @Column(name = "wellness_suggestions_enabled")
    private boolean wellnessSuggestionsEnabled = true;

    public UserPreference() {}

    public UserPreference(String userId) {
        this.userId = userId;
    }

    public UserPreference(String userId, String reminderFrequency, String smartNotifIntensity, String quietHoursStart, String quietHoursEnd, boolean soundEnabled, boolean vibrationEnabled, boolean moodSupportEnabled, boolean wellnessSuggestionsEnabled) {
        this.userId = userId;
        this.reminderFrequency = reminderFrequency;
        this.smartNotifIntensity = smartNotifIntensity;
        this.quietHoursStart = quietHoursStart;
        this.quietHoursEnd = quietHoursEnd;
        this.soundEnabled = soundEnabled;
        this.vibrationEnabled = vibrationEnabled;
        this.moodSupportEnabled = moodSupportEnabled;
        this.wellnessSuggestionsEnabled = wellnessSuggestionsEnabled;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getReminderFrequency() {
        return reminderFrequency;
    }

    public void setReminderFrequency(String reminderFrequency) {
        this.reminderFrequency = reminderFrequency;
    }

    public String getSmartNotifIntensity() {
        return smartNotifIntensity;
    }

    public void setSmartNotifIntensity(String smartNotifIntensity) {
        this.smartNotifIntensity = smartNotifIntensity;
    }

    public String getQuietHoursStart() {
        return quietHoursStart;
    }

    public void setQuietHoursStart(String quietHoursStart) {
        this.quietHoursStart = quietHoursStart;
    }

    public String getQuietHoursEnd() {
        return quietHoursEnd;
    }

    public void setQuietHoursEnd(String quietHoursEnd) {
        this.quietHoursEnd = quietHoursEnd;
    }

    public boolean isSoundEnabled() {
        return soundEnabled;
    }

    public void setSoundEnabled(boolean soundEnabled) {
        this.soundEnabled = soundEnabled;
    }

    public boolean isVibrationEnabled() {
        return vibrationEnabled;
    }

    public void setVibrationEnabled(boolean vibrationEnabled) {
        this.vibrationEnabled = vibrationEnabled;
    }

    public boolean isMoodSupportEnabled() {
        return moodSupportEnabled;
    }

    public void setMoodSupportEnabled(boolean moodSupportEnabled) {
        this.moodSupportEnabled = moodSupportEnabled;
    }

    public boolean isWellnessSuggestionsEnabled() {
        return wellnessSuggestionsEnabled;
    }

    public void setWellnessSuggestionsEnabled(boolean wellnessSuggestionsEnabled) {
        this.wellnessSuggestionsEnabled = wellnessSuggestionsEnabled;
    }
}
