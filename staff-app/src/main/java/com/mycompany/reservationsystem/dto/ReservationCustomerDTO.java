package com.mycompany.reservationsystem.dto;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class ReservationCustomerDTO {
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("hh:mm a");
    private static final DateTimeFormatter PARSE_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final DateTimeFormatter PARSE_FORMATTER_SHORT = DateTimeFormatter.ofPattern("HH:mm");

    private Long id;
    private String reference;
    private String customerName;
    private String phoneNumber;
    private int totalVisits;
    private double totalSpent;
    private String lastVisit;
    private String registrationTime;
    private String status;

    public ReservationCustomerDTO() {}

    public ReservationCustomerDTO(Long id, String reference, String customerName, String phoneNumber, int totalVisits, double totalSpent, String lastVisit) {
        this.id = id;
        this.reference = reference;
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

    public String getRegistrationTime() { return registrationTime; }
    public void setRegistrationTime(String registrationTime) { this.registrationTime = registrationTime; }

    public String getStatus() { return status != null ? status : ""; }
    public void setStatus(String status) { this.status = status; }

    public String getSales() { return String.format("%.2f", totalSpent); }
    public String getTime() {
        String timePart = null;
        if (registrationTime != null && !registrationTime.isEmpty()) {
            timePart = registrationTime;
        } else if (lastVisit != null && lastVisit.length() > 10) {
            timePart = lastVisit.substring(11);
        }

        if (timePart != null) {
            try {
                // Remove nanoseconds if present
                if (timePart.contains(".")) {
                    timePart = timePart.split("\\.")[0];
                }
                LocalTime lt;
                if (timePart.length() > 5) {
                    lt = LocalTime.parse(timePart, PARSE_FORMATTER);
                } else {
                    lt = LocalTime.parse(timePart, PARSE_FORMATTER_SHORT);
                }
                return lt.format(TIME_FORMATTER);
            } catch (Exception e) {
                return timePart;
            }
        }
        return "";
    }
    public String getDate() {
        if (lastVisit == null || lastVisit.isEmpty()) return "";
        if (lastVisit.length() > 10) {
            return lastVisit.substring(0, 10);
        }
        return lastVisit;
    }
    public String getCustomerPhone() { return phoneNumber; }
}
