package com.mycompany.reservationsystem.dto;

import java.math.BigDecimal;

public class TableUsageReportDTO {

    private String tableNo;
    private Long totalReservation;
    private Long totalCustomer;
    private BigDecimal totalSales;

    public TableUsageReportDTO(String tableNo, Long totalReservation, Long totalCustomer, BigDecimal totalSales) {
        this.tableNo = tableNo;
        this.totalReservation = totalReservation;
        this.totalCustomer = totalCustomer;
        this.totalSales = totalSales;
    }

    // Getters and setters
    public String getTableNo() {
        return tableNo;
    }

    public void setTableNo(String tableNo) {
        this.tableNo = tableNo;
    }

    public Long getTotalReservation() {
        return totalReservation;
    }

    public void setTotalReservation(Long totalReservation) {
        this.totalReservation = totalReservation;
    }

    public Long getTotalCustomer() {
        return totalCustomer;
    }

    public void setTotalCustomer(Long totalCustomer) {
        this.totalCustomer = totalCustomer;
    }

    public BigDecimal getTotalSales() {
        return totalSales;
    }

    public void setTotalSales(BigDecimal totalSales) {
        this.totalSales = totalSales;
    }
}