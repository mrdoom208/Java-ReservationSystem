package com.mycompany.reservationsystem.model;

import jakarta.persistence.*;

@Entity
@Table(
        uniqueConstraints = @UniqueConstraint(columnNames = {"position", "permission_id"})
)
public class PositionPermission {

    @Id
    @GeneratedValue
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private User.Position position;

    @ManyToOne(optional = false)
    private Permission permission;

    // Default constructor required by JPA
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
