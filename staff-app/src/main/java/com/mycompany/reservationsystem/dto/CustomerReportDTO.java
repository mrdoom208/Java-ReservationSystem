package com.mycompany.reservationsystem.dto;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.math.BigDecimal;

public class CustomerReportDTO {
    private Long customerId;
    private String customerName;
    private String phoneNumber;
    private int totalVisits;
    private double totalSpent;
    private double averageSpend;
    private String lastVisit;
    private boolean selected;
    private Long id;
    private String reference;
    private String status;
    private String date;

    public CustomerReportDTO() {}

    public CustomerReportDTO(Long customerId, String customerName, String phoneNumber, int totalVisits, double totalSpent, String lastVisit) {
        this.customerId = customerId;
        this.customerName = customerName;
        this.phoneNumber = phoneNumber;
        this.totalVisits = totalVisits;
        this.totalSpent = totalSpent;
        this.lastVisit = lastVisit;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getReference() { return reference; }
    public void setReference(String reference) { this.reference = reference; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getPhone() { return phoneNumber; }
    public void setPhone(String phone) { this.phoneNumber = phone; }

    public int getTotalVisits() { return totalVisits; }
    public void setTotalVisits(int totalVisits) { this.totalVisits = totalVisits; }

    public Long getTotalReservation() { return (long) totalVisits; }

    public double getTotalSpent() { return totalSpent; }
    public void setTotalSpent(double totalSpent) { this.totalSpent = totalSpent; }

    public double getAverageSpend() { return averageSpend; }
    public void setAverageSpend(double averageSpend) { this.averageSpend = averageSpend; }

    public BigDecimal getTotalSales() { return BigDecimal.valueOf(totalSpent); }

    public double getAverageSales() { return totalVisits > 0 ? totalSpent / totalVisits : 0; }

    public String getLastVisit() { return lastVisit; }
    public void setLastVisit(String lastVisit) { this.lastVisit = lastVisit; }

    public boolean isSelected() { return selected; }
    public void setSelected(Boolean selected) { this.selected = selected != null && selected; }

    public SimpleBooleanProperty selectedProperty() {
        return new SimpleBooleanProperty(selected);
    }
}
