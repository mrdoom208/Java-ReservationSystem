package com.mycompany.reservationsystem.model;

import jakarta.persistence.*;

@Entity
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String account; // the reference or username

    private String code; // e.g., CONFIRM_RESERVATION
    private String message;


    private boolean sent = false;
    // optionally: store timestamp
    private long timestamp = System.currentTimeMillis();

    // Store any serialized DTO info if needed

    // getters and setters
    public Long getId() { return id; }
    public String getAccount() { return account; }
    public void setAccount(String account) { this.account = account; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public boolean isSent() { return sent; }
    public void setSent(boolean sent) { this.sent = sent; }
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}
