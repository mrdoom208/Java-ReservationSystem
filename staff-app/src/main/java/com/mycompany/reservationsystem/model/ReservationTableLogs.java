/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.reservationsystem.model;

import com.mycompany.reservationsystem.dto.ManageTablesDTO;

import java.math.BigDecimal;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 *
 * @author formentera
 */
public class ReservationTableLogs {
    
     Long id;

    String action;
    String timestamp;
    
     String customer;
     int pax;
     String prefer;
     String phone;
     String email;
     String status;
     String reference;
     Long tableid;
     LocalTime reservationPendingtime;
     LocalTime reservationConfirmtime;
     LocalTime reservationSeatedtime;
     LocalTime reservationCompletetime;
     String tableNo;
     int tablecapacity;

     BigDecimal Sales;
      
      public BigDecimal getSales() { return Sales; }
      public void setSales(BigDecimal Sales) { this.Sales = Sales; }
     
     String tablelocation;
      LocalTime tablestarttime;
      LocalTime tableendtime;
      LocalTime occupiedTime;
      LocalDate date;

    public ReservationTableLogs() {
    }

     
    public ReservationTableLogs(ManageTablesDTO dto) {
    this.customer = dto.getCustomer();
    this.pax = dto.getPax();
    this.prefer = dto.getPrefer();
    this.phone = dto.getPhone();
    this.email = dto.getEmail();
    this.status = dto.getStatus();
    this.reference = dto.getReference();
    this.tableid = dto.getTableId();
    this.reservationPendingtime = dto.getReservationPendingtime();
    this.reservationConfirmtime = dto.getReservationConfirmtime();
    this.reservationSeatedtime = dto.getReservationSeatedtime();
    this.reservationCompletetime = dto.getReservationCompletetime();
    this.tableNo = dto.getTableNo();
    this.tablecapacity = dto.getCapacity();
    this.tablelocation = dto.getLocation();
     this.tablestarttime = dto.getTablestarttime();
     this.tableendtime = dto.getTableendtime();
     this.occupiedTime = dto.getReservationSeatedtime();
     this.date = dto.getDate();
     this.Sales = dto.getSales();
}
    
    public String getCustomer() {
        return customer;
    }

    public void setCustomer(String customer) {
        this.customer = customer;
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

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public Long getTableid() {
        return tableid;
    }

    public void setTableid(Long tableid) {
        this.tableid = tableid;
    }

    public LocalTime getReservationPendingtime() {
        return reservationPendingtime;
    }

    public void setReservationPendingtime(LocalTime reservationPendingtime) {
        this.reservationPendingtime = reservationPendingtime;
    }
    public LocalTime getReservationConfirmtime(){
        return reservationConfirmtime;
    }
    public void setReservationConfirmtime(LocalTime reservationConfirmtime) {
        this.reservationConfirmtime = reservationConfirmtime;
    }
    
    public LocalTime getReservationStarttime() {
        return reservationConfirmtime;
    }

    public void setReservationStarttime(LocalTime reservationStarttime) {
        this.reservationConfirmtime = reservationStarttime;
    }

    public LocalTime getReservationCompletetime() {
        return reservationCompletetime;
    }

    public void setReservationCompletetime(LocalTime reservationCompletetime) {
        this.reservationCompletetime = reservationCompletetime;
    }

    public String getTableNo() {
        return tableNo;
    }

    public void setTableNo(String tableNo) {
        this.tableNo = tableNo;
    }

    public int getTablecapacity() {
        return tablecapacity;
    }

    public void setTablecapacity(int tablecapacity) {
        this.tablecapacity = tablecapacity;
    }

    public String getTablelocation() {
        return tablelocation;
    }

    public void setTablelocation(String tablelocation) {
        this.tablelocation = tablelocation;
    }

    public LocalTime getTablestarttime() {
        return tablestarttime;
    }

    public void setTablestarttime(LocalTime tablestarttime) {
        this.tablestarttime = tablestarttime;
    }

    public LocalTime getTableendtime() {
        return tableendtime;
    }

    public void setTableendtime(LocalTime tableendtime) {
        this.tableendtime = tableendtime;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }
    
    public LocalTime getOccupiedTime() { return occupiedTime; }
    public void setOccupiedTime(LocalTime occupiedTime) { this.occupiedTime = occupiedTime; }

    public LocalTime getReservationSeatedtime() {return reservationSeatedtime;}

    public void setReservationSeatedtime(LocalTime reservationStarttime) {this.reservationSeatedtime = reservationStarttime;}

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }

    
}
