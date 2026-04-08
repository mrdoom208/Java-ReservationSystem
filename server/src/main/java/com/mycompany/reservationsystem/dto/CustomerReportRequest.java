package com.mycompany.reservationsystem.dto;

import java.time.LocalDate;

public class CustomerReportRequest {
    private String phone;
    private LocalDate from;
    private LocalDate to;

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public LocalDate getFrom() {
        return from;
    }

    public void setFrom(LocalDate from) {
        this.from = from;
    }

    public LocalDate getTo() {
        return to;
    }

    public void setTo(LocalDate to) {
        this.to = to;
    }
}
