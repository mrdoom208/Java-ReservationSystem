package com.mycompany.reservationsystem.dto;

public class ReservationCustomerDTO {
    private Long id;
    private String customerName;
    private String phoneNumber;
    private int totalVisits;
    private double totalSpent;
    private String lastVisit;
    private String status;

    public ReservationCustomerDTO() {}

    public ReservationCustomerDTO(Long id, String customerName, String phoneNumber, int totalVisits, double totalSpent, String lastVisit) {
        this.id = id;
        this.customerName = customerName;
        this.phoneNumber = phoneNumber;
        this.totalVisits = totalVisits;
        this.totalSpent = totalSpent;
        this.lastVisit = lastVisit;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public int getTotalVisits() { return totalVisits; }
    public void setTotalVisits(int totalVisits) { this.totalVisits = totalVisits; }

    public double getTotalSpent() { return totalSpent; }
    public void setTotalSpent(double totalSpent) { this.totalSpent = totalSpent; }

    public String getLastVisit() { return lastVisit; }
    public void setLastVisit(String lastVisit) { this.lastVisit = lastVisit; }

    public String getReference() { return id != null ? "RSV-" + String.format("%05d", id) : ""; }
    public String getStatus() { return status != null ? status : ""; }
    public void setStatus(String status) { this.status = status; }
    public String getSales() { return String.format("%.2f", totalSpent); }
    public String getReservationPendingtime() { return lastVisit != null ? lastVisit : ""; }
    public String getCustomerPhone() { return phoneNumber; }
    public String getDate() { return lastVisit != null ? lastVisit : ""; }
}
