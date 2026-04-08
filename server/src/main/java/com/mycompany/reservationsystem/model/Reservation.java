/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.reservationsystem.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 *
 * @author formentera
 */
@Entity
@Table(indexes = {
    @Index(name = "idx_reservation_status", columnList = "status"),
    @Index(name = "idx_reservation_reference", columnList = "reference"),
    @Index(name = "idx_reservation_date", columnList = "date")
})
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int pax;
    private String prefer;
    private String status;
    private String reference;
    private LocalDate date;

    @Column(precision = 19, scale = 2)
    private BigDecimal sales;

    private LocalTime reservationPendingtime;
    private LocalTime reservationNotifiedtime;
    private LocalTime reservationConfirmtime;
    private LocalTime reservationCancelledtime;
    private LocalTime reservationSeatedtime;
    private LocalTime reservationCompletetime;
    private LocalTime reservationNoshowtime;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "customer_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Customer customer;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "table_id", nullable = true)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private ManageTables table;

    // ============================
    //       GETTERS & SETTERS
    // ============================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getPax() {
        return pax;
    }

    public void setPax(int pax) {
        this.pax = pax;
    }

    public String getPrefer() {
        return prefer;
    }

    public void setPrefer(String prefer) {
        this.prefer = prefer;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public BigDecimal getSales(){ return sales;}
    public void setSales(BigDecimal sales){this.sales = sales;}

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public LocalTime getReservationPendingtime() {
        return reservationPendingtime;
    }

    public void setReservationPendingtime(LocalTime reservationPendingtime) {
        this.reservationPendingtime = reservationPendingtime;
    }

    public LocalTime getReservationNotifiedtime() {
        return reservationNotifiedtime;
    }

    public void setReservationNotifiedtime(LocalTime reservationNotifiedtime) {
        this.reservationNotifiedtime = reservationNotifiedtime;
    }

    public LocalTime getReservationConfirmtime() {
        return reservationConfirmtime;
    }

    public void setReservationConfirmtime(LocalTime reservationConfirmtime) {
        this.reservationConfirmtime = reservationConfirmtime;
    }

    public LocalTime getReservationCancelledtime() {
        return reservationCancelledtime;
    }

    public void setReservationCancelledtime(LocalTime reservationCancelledtime) {
        this.reservationCancelledtime = reservationCancelledtime;
    }
    public LocalTime getReservationNoshowtime() {
        return reservationNoshowtime;
    }

    public void setReservationNoshowtime(LocalTime reservationnoshowtime) {
        this.reservationNoshowtime = reservationnoshowtime;
    }

    public LocalTime getReservationSeatedtime() {
        return reservationSeatedtime;
    }

    public void setReservationSeatedtime(LocalTime reservationSeatedtime) {
        this.reservationSeatedtime = reservationSeatedtime;
    }

    public LocalTime getReservationCompletetime() {
        return reservationCompletetime;
    }

    public void setReservationCompletetime(LocalTime reservationCompletetime) {
        this.reservationCompletetime = reservationCompletetime;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public ManageTables getTable() {
        return table;
    }

    public void setTable(ManageTables table) {
        this.table = table;
    }
}