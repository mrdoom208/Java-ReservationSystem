package com.mycompany.reservationsystem.dto;

public class NotificationDTO {
    private Long id;
    private String message;
    private String account;
    private String code;

    public NotificationDTO() {}

    public NotificationDTO(com.mycompany.reservationsystem.model.Notification n) {
        this.id = n.getId();
        this.message = n.getMessage();
        this.account = n.getAccount();
        this.code = n.getCode();
    }

    // getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getAccount() { return account; }
    public void setAccount(String account) { this.account = account; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
}
