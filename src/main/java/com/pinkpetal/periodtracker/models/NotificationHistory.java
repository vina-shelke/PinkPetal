package com.pinkpetal.periodtracker.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notification_history")
public class NotificationHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "user_id", nullable = false, length = 50)
    private String userId;

    @Column(nullable = false)
    private LocalDateTime date;

    @Column(nullable = false, length = 255)
    private String content;

    @Column(nullable = false, length = 50)
    private String type; // e.g., "Emotional", "Hydration", "Hygiene", "Comfort", "Rest", "Nutrition"

    @Column(name = "read_status")
    private Boolean readStatus = false;

    public NotificationHistory() {}

    public NotificationHistory(String userId, LocalDateTime date, String content, String type) {
        this.userId = userId;
        this.date = date;
        this.content = content;
        this.type = type;
        this.readStatus = false;
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

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isReadStatus() {
        return readStatus != null ? readStatus : false;
    }

    public void setReadStatus(boolean readStatus) {
        this.readStatus = readStatus;
    }
}
