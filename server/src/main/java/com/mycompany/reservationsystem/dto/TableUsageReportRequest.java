package com.mycompany.reservationsystem.dto;

import java.time.LocalDate;

public class TableUsageReportRequest {
    private LocalDate date;

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }
}
