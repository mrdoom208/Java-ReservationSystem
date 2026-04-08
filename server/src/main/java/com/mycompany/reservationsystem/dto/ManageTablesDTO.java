package com.mycompany.reservationsystem.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

public class ManageTablesDTO {

    @JsonProperty("tableNo")
    private String tableNo;
    private String status;
    private Integer capacity;
    private String customer;
    private Integer pax;
    
    private String location;
    private String prefer;
    private String customerStatus;
    private String phone;                // <-- added
    private String email;                // <-- added
    
    private String reference;
    private BigDecimal sales;
    private LocalTime reservationPendingtime;
    private LocalTime reservationConfirmtime;
    private LocalTime reservationSeatedtime;
    private LocalTime reservationCompletetime;
    
    @JsonProperty("tableId")
    private Long tableId;
    private LocalTime tablestarttime;
    private LocalTime tableendtime;
    private LocalDate date;

    public ManageTablesDTO() {
    }

    public ManageTablesDTO(
            String tableNo,
            String status,
            Integer capacity,
            String customer,
            Integer pax,
            BigDecimal sales,
            
            String location,
            String prefer,
            String customerStatus,   
            String reference,
            String phone,
            
            String email,
            LocalTime reservationPendingtime,
            LocalTime reservationConfirmtime,
            LocalTime reservationSeatedtime,
            LocalTime reservationCompletetime,
            
            Long tableId,
            LocalTime tablestarttime,
            LocalTime tableendtime,
            LocalDate date
    ) {
        this.tableNo = tableNo;
        this.status = status;
        this.capacity = capacity;
        this.customer = customer;
        this.pax = pax;
        this.location = location;
        this.sales = sales;

        this.prefer = prefer;
        this.customerStatus = customerStatus;  // <-- added
        this.phone = phone;
        this.email = email;
        this.reference = reference;
        this.reservationPendingtime = reservationPendingtime;
        this.reservationConfirmtime = reservationConfirmtime;
        this.reservationSeatedtime = reservationSeatedtime;
        this.reservationCompletetime = reservationCompletetime;
        this.tableId = tableId;
        this.tablestarttime = tablestarttime;
        this.tableendtime = tableendtime;
        this.date = date;
    }

    // Getters
    @JsonProperty("tableNo")
    public String getTableNo() { return tableNo; }
    @JsonProperty("status")
    public String getStatus() { return status; }
    @JsonProperty("capacity")
    public Integer getCapacity() { return capacity; }
    @JsonProperty("customer")
    public String getCustomer() { return customer; }
    @JsonProperty("pax")
    public Integer getPax() { return pax; }
    @JsonProperty("location")
    public String getLocation(){ return location; }
    @JsonProperty("sales")
    public BigDecimal getSales(){return sales;}
    @JsonProperty("prefer")
    public String getPrefer() { return prefer; }
    @JsonProperty("customerStatus")
    public String getCustomerStatus() { return customerStatus; } // <-- added
    @JsonProperty("phone")
    public String getPhone() { return phone; }
    @JsonProperty("email")
    public String getEmail() { return email; }
    @JsonProperty("reference")
    public String getReference() { return reference; }
    @JsonProperty("reservationPendingtime")
    public LocalTime getReservationPendingtime() { return reservationPendingtime; }
    @JsonProperty("reservationConfirmtime")
    public LocalTime getReservationConfirmtime() { return reservationConfirmtime; }
    @JsonProperty("reservationSeatedtime")
    public LocalTime getReservationSeatedtime() { return reservationSeatedtime; }
    @JsonProperty("reservationCompletetime")
    public LocalTime getReservationCompletetime() { return reservationCompletetime; }
    @JsonProperty("tableId")
    public Long getTableId() { return tableId; }
    @JsonProperty("tablestarttime")
    public LocalTime getTablestarttime() { return tablestarttime; }
    @JsonProperty("tableendtime")
    public LocalTime getTableendtime() { return tableendtime; }
    @JsonProperty("date")
    public LocalDate getDate() { return date; }

    // Setters
    @JsonProperty("tableNo")
    public void setTableNo(String tableNo) { this.tableNo = tableNo; }
    @JsonProperty("status")
    public void setStatus(String status) { this.status = status; }
    @JsonProperty("capacity")
    public void setCapacity(Integer capacity) { this.capacity = capacity; }
    @JsonProperty("customer")
    public void setCustomer(String customer) { this.customer = customer; }
    @JsonProperty("pax")
    public void setPax(Integer pax) { this.pax = pax; }
    @JsonProperty("location")
    public void setLocation(String location) { this.location = location; }
    @JsonProperty("sales")
    public void setSales(BigDecimal sales){this.sales = sales;}
    @JsonProperty("prefer")
    public void setPrefer(String prefer) { this.prefer = prefer; }
    @JsonProperty("customerStatus")
    public void setCustomerStatus(String customerStatus) { this.customerStatus = customerStatus; } // <-- added
    @JsonProperty("phone")
    public void setPhone(String phone) { this.phone = phone; }
    @JsonProperty("email")
    public void setEmail(String email) { this.email = email; }
    @JsonProperty("reference")
    public void setReference(String reference) { this.reference = reference; }
    @JsonProperty("reservationPendingtime")
    public void setReservationPendingtime(LocalTime reservationPendingtime) { this.reservationPendingtime = reservationPendingtime; }
    @JsonProperty("reservationConfirmtime")
    public void setReservationConfirmtime(LocalTime reservationConfirmtime) { this.reservationConfirmtime = reservationConfirmtime; }
    @JsonProperty("reservationSeatedtime")
    public void setReservationSeatedtime(LocalTime reservationSeatedtime) { this.reservationSeatedtime = reservationSeatedtime; }
    @JsonProperty("reservationCompletetime")
    public void setReservationCompletetime(LocalTime reservationCompletetime) { this.reservationCompletetime = reservationCompletetime; }
    @JsonProperty("tableId")
    public void setTableId(Long tableId) { this.tableId = tableId; }
    @JsonProperty("tablestarttime")
    public void setTablestarttime(LocalTime tablestarttime) { this.tablestarttime = tablestarttime; }
    @JsonProperty("tableendtime")
    public void setTableendtime(LocalTime tableendtime) { this.tableendtime = tableendtime; }
    @JsonProperty("date")
    public void setDate(LocalDate date) { this.date = date; }
}