package com.mycompany.reservationsystem.dto;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class CustomerReportDTO {

    private boolean selected;
    private String phone;
    private Long totalReservation;
    private BigDecimal totalSales;
    private Double averageSales;

    public CustomerReportDTO(String phone, Long totalReservation, BigDecimal totalSales, Double averageSales) {
        this.phone = phone;
        this.totalReservation = totalReservation != null ? totalReservation : 0L;
        this.totalSales = totalSales != null
                ? new BigDecimal(totalSales.toString()).setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
        this.averageSales = averageSales != null
                ? Math.round(averageSales.doubleValue() * 100.0) / 100.0
                : 0.00;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean value) {
        this.selected = value;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Long getTotalReservation() {
        return totalReservation;
    }

    public void setTotalReservation(Long totalReservation) {
        this.totalReservation = totalReservation;
    }

    public BigDecimal getTotalSales() {
        return totalSales;
    }

    public void setTotalSales(BigDecimal totalSales) {
        this.totalSales = totalSales;
    }

    public Double getAverageSales() {
        return averageSales;
    }

    public void setAverageSales(Double averageSales) {
        this.averageSales = averageSales;
    }
}
