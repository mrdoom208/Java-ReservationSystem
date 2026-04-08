package com.mycompany.reservationsystem.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ActivityLog {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("user")
    private String user;

    @JsonProperty("position")
    private String position;

    @JsonProperty("module")
    private String module;

    @JsonProperty("action")
    private String action;

    @JsonProperty("description")
    private String description;

    @JsonProperty("timestamp")
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