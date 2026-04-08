package com.mycompany.reservationsystem.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

public class ManageTablesDTO {
    private Long id;
    private Long tableId;
    private String tableNumber;
    private int capacity;
    private String status;
    private String location;
    private String position;
    private String currentCustomer;
    private String reservationTime;
    private String customer;
    private String prefer;
    private String phone;
    private String email;
    private Integer pax;
    private LocalTime tablestarttime;
    private String reference;
    private LocalDate date;
    private LocalTime reservationSeatedtime;
    private LocalTime reservationCompletetime;
    private LocalTime reservationPendingtime;
    private LocalTime reservationConfirmtime;
    private LocalTime tableendtime;
    private BigDecimal sales;

    public ManageTablesDTO() {}

    public ManageTablesDTO(Long id, String tableNumber, int capacity, String status, String location, String position) {
        this.id = id;
        this.tableNumber = tableNumber;
        this.capacity = capacity;
        this.status = status;
        this.location = location;
        this.position = position;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getTableId() { return tableId != null ? tableId : id; }
    public void setTableId(Long tableId) { this.tableId = tableId; }

    public String getTableNumber() { return tableNumber; }
    public void setTableNumber(String tableNumber) { this.tableNumber = tableNumber; }

    public String getTableNo() { return tableNumber; }
    public void setTableNo(String tableNo) { this.tableNumber = tableNo; }

    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getPosition() { return position; }
    public void setPosition(String position) { this.position = position; }

    public String getCurrentCustomer() { return currentCustomer; }
    public void setCurrentCustomer(String currentCustomer) { this.currentCustomer = currentCustomer; }

    public String getReservationTime() { return reservationTime; }
    public void setReservationTime(String reservationTime) { this.reservationTime = reservationTime; }

    public String getCustomer() { return customer; }
    public void setCustomer(String customer) { this.customer = customer; }

    public String getPrefer() { return prefer; }
    public void setPrefer(String prefer) { this.prefer = prefer; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public Integer getPax() { return pax; }
    public void setPax(Integer pax) { this.pax = pax; }

    public LocalTime getTablestarttime() { return tablestarttime; }
    public void setTablestarttime(LocalTime tablestarttime) { this.tablestarttime = tablestarttime; }

    public String getReference() { return reference; }
    public void setReference(String reference) { this.reference = reference; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public LocalTime getReservationSeatedtime() { return reservationSeatedtime; }
    public void setReservationSeatedtime(LocalTime reservationSeatedtime) { this.reservationSeatedtime = reservationSeatedtime; }

    public LocalTime getReservationCompletetime() { return reservationCompletetime; }
    public void setReservationCompletetime(LocalTime reservationCompletetime) { this.reservationCompletetime = reservationCompletetime; }

    public LocalTime getReservationPendingtime() { return reservationPendingtime; }
    public void setReservationPendingtime(LocalTime reservationPendingtime) { this.reservationPendingtime = reservationPendingtime; }

    public LocalTime getReservationConfirmtime() { return reservationConfirmtime; }
    public void setReservationConfirmtime(LocalTime reservationConfirmtime) { this.reservationConfirmtime = reservationConfirmtime; }

    public LocalTime getTableendtime() { return tableendtime; }
    public void setTableendtime(LocalTime tableendtime) { this.tableendtime = tableendtime; }

    public BigDecimal getSales() { return sales; }
    public void setSales(BigDecimal sales) { this.sales = sales; }
}
