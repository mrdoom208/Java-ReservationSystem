package com.mycompany.reservationsystem.model;
import jakarta.persistence.*;

@Entity
public class Permission {
    @Id @GeneratedValue
    private Long id;

    @Column(unique = true)
    private String code;

    private String module;

    public Permission() {}

    public Permission(String code, String module) {
        this.code = code;
        this.module = module;
    }


    // ✅ Constructor to create Permission with code
    public Permission(String code) {
        this.code = code;
    }

    // Getters & setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getModule() { return module; } // <--- getter
    public void setModule(String module) { this.module = module; } // <--- setter
}