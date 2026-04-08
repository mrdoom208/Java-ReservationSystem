package com.mycompany.reservationsystem.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "activity_logs")
public class ActivityLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "username")
    private String user;        // username of the person performing the action

    private String position;    // new field: role/position of the user

    private String module;

    private String action;

    @Column(columnDefinition = "TEXT")
    private String description;

    private LocalDateTime timestamp;

    public ActivityLog() {}

    public ActivityLog(String user, String position, String module, String action, String description, LocalDateTime timestamp) {
        this.user = user;
        this.position = position;
        this.module = module;
        this.action = action;
        this.description = description;
        this.timestamp = timestamp;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public String getUser() { return user; }
    public String getPosition() { return position; }
    public String getModule() { return module; }
    public String getAction() { return action; }
    public String getDescription() { return description; }
    public LocalDateTime getTimestamp() { return timestamp; }

    public void setId(Long id) { this.id = id; }
    public void setUser(String user) { this.user = user; }
    public void setPosition(String position) { this.position = position; }
    public void setModule(String module) { this.module = module; }
    public void setAction(String action) { this.action = action; }
    public void setDescription(String description) { this.description = description; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}