package com.mycompany.reservationsystem.dto;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

public class SalesReportsDTO {
    private LocalDate date;
    private double sales;
    private int reservationCount;
    private long totalCustomer;

    public SalesReportsDTO() {}

    public SalesReportsDTO(LocalDate date, long totalCustomer, long totalReservation, BigDecimal totalSales) {
        this.date = date;
        this.totalCustomer = totalCustomer;
        this.reservationCount = (int) totalReservation;
        this.sales = totalSales != null ? totalSales.doubleValue() : 0;
    }

    public SalesReportsDTO(String date, double sales, int reservationCount) {
        this.date = date != null ? LocalDate.parse(date) : null;
        this.sales = sales;
        this.reservationCount = reservationCount;
    }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public double getSales() { return sales; }
    public void setSales(double sales) { this.sales = sales; }

    public double getTotalSales() { return sales; }
    public void setTotalSales(double sales) { this.sales = sales; }

    public int getReservationCount() { return reservationCount; }
    public void setReservationCount(int reservationCount) { this.reservationCount = reservationCount; }

    public int getTotalReservation() { return reservationCount; }

    public long getTotalCustomer() { return totalCustomer; }
    public void setTotalCustomer(long totalCustomer) { this.totalCustomer = totalCustomer; }
}
