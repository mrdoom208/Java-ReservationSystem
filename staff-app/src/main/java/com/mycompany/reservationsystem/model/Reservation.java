/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.reservationsystem.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 *
 * @author formentera
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Reservation {

    private Long id;
    private int pax;
    private String prefer;
    private String status;
    private String reference;
    private LocalDate date;
    private BigDecimal sales;
    private LocalTime reservationPendingtime;
    private LocalTime reservationNotifiedtime;
    private LocalTime reservationConfirmtime;
    private LocalTime reservationCancelledtime;
    private LocalTime reservationSeatedtime;
    private LocalTime reservationCompletetime;
    private LocalTime reservationNoshowtime;
    private Customer customer;
    private Long customerId;
    private ManageTables table;

    // ============================
    //       GETTERS & SETTERS
    // ============================

    @JsonProperty("id")
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    @JsonProperty("pax")
    public int getPax() { return pax; }
    public void setPax(int pax) { this.pax = pax; }

    @JsonProperty("prefer")
    public String getPrefer() { return prefer; }
    public void setPrefer(String prefer) { this.prefer = prefer; }

    @JsonProperty("status")
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    @JsonProperty("sales")
    public BigDecimal getSales() { return sales; }
    public void setSales(BigDecimal sales) { this.sales = sales; }

    @JsonProperty("reference")
    public String getReference() { return reference; }
    public void setReference(String reference) { this.reference = reference; }

    @JsonProperty("date")
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    @JsonProperty("reservationPendingtime")
    public LocalTime getReservationPendingtime() { return reservationPendingtime; }
    public void setReservationPendingtime(LocalTime reservationPendingtime) { this.reservationPendingtime = reservationPendingtime; }

    @JsonProperty("reservationNotifiedtime")
    public LocalTime getReservationNotifiedtime() { return reservationNotifiedtime; }
    public void setReservationNotifiedtime(LocalTime reservationNotifiedtime) { this.reservationNotifiedtime = reservationNotifiedtime; }

    @JsonProperty("reservationConfirmtime")
    public LocalTime getReservationConfirmtime() { return reservationConfirmtime; }
    public void setReservationConfirmtime(LocalTime reservationConfirmtime) { this.reservationConfirmtime = reservationConfirmtime; }

    @JsonProperty("reservationCancelledtime")
    public LocalTime getReservationCancelledtime() { return reservationCancelledtime; }
    public void setReservationCancelledtime(LocalTime reservationCancelledtime) { this.reservationCancelledtime = reservationCancelledtime; }

    @JsonProperty("reservationSeatedtime")
    public LocalTime getReservationSeatedtime() { return reservationSeatedtime; }
    public void setReservationSeatedtime(LocalTime reservationSeatedtime) { this.reservationSeatedtime = reservationSeatedtime; }

    @JsonProperty("reservationCompletetime")
    public LocalTime getReservationCompletetime() { return reservationCompletetime; }
    public void setReservationCompletetime(LocalTime reservationCompletetime) { this.reservationCompletetime = reservationCompletetime; }

    @JsonProperty("reservationNoshowtime")
    public LocalTime getReservationNoshowtime() { return reservationNoshowtime; }
    public void setReservationNoshowtime(LocalTime reservationNoshowtime) { this.reservationNoshowtime = reservationNoshowtime; }

    @JsonProperty("customer")
    public Customer getCustomer() { return customer; }
    public void setCustomer(Customer customer) { this.customer = customer; }

    @JsonProperty("customerId")
    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }

    @JsonProperty("table")
    public ManageTables getTable() { return table; }
    public void setTable(ManageTables table) { this.table = table; }
}
