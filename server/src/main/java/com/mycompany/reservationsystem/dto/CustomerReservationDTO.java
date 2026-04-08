package com.mycompany.reservationsystem.dto;

public class CustomerReservationDTO {

    private String name;
    private String phone;
    private String email;
    private int pax;
    private String prefer;

    // ===============================
    //           GETTERS
    // ===============================

    public String getName() {
        return name;
    }

    public String getPhone() {
        return phone;
    }

    public String getEmail() {
        return email;
    }

    public int getPax() {
        return pax;
    }

    public String getPrefer() {
        return prefer;
    }

    // ===============================
    //           SETTERS
    // ===============================

    public void setName(String name) {
        this.name = name;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPax(int pax) {
        this.pax = pax;
    }

    public void setPrefer(String prefer) {
        this.prefer = prefer;
    }
}