package com.mycompany.reservationsystem.model;

public class PositionPermission {

    private Long id;

    private User.Position position;

    private Permission permission;

    public PositionPermission() {}

    public PositionPermission(User.Position position, Permission permission) {
        this.position = position;
        this.permission = permission;
    }

    // Getters & setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User.Position getPosition() { return position; }
    public void setPosition(User.Position position) { this.position = position; }

    public Permission getPermission() { return permission; }
    public void setPermission(Permission permission) { this.permission = permission; }
}
