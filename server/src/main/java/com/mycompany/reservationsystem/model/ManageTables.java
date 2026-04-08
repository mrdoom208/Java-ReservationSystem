/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.reservationsystem.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;

import java.sql.Time;
import java.time.LocalTime;
import java.util.List;

/**
 *
 * @author formentera
 */
@Entity
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class ManageTables {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    
    @JsonProperty("tableNo")
    @Column(unique = true)
    String tableNo;
    
    @JsonProperty("capacity")
    int capacity;
    
    @JsonProperty("status")
    String status;
    
    @JsonProperty("location")
    String location;
    
    @JsonProperty("customer")
    String customer;
    
    @JsonProperty("customerPhone")
    String customerPhone;
    
    @JsonProperty("customerEmail")
    String customerEmail;
    
    @JsonProperty("reference")
    String reference;
    
    @JsonProperty("pax")
    Integer pax;
    
    @JsonProperty("tablestarttime")
    LocalTime tablestarttime;
    
    @JsonProperty("tableendtime")
    LocalTime tableendtime;
    
    @OneToMany(mappedBy = "table")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    @JsonIgnore
    private List<Reservation> reservations;
    
    public Long getId(){return id;}
    public void setId(Long id){this.id = id;}
    
    public String getTableNo(){return tableNo;}
    public void setTableNo(String tableNo){this.tableNo = tableNo;}
    
    public String getStatus(){return status;}
    public void setStatus(String Status){this.status = Status;}
    
    public int getCapacity(){return capacity;}
    public void setCapacity(int Capacity){this.capacity = Capacity;}
    
    public LocalTime getTablestarttime() {return tablestarttime;}
    public void setTablestarttime(LocalTime tablestarttime) {this.tablestarttime = tablestarttime;}

    public LocalTime getTableendtime() {return tableendtime;}
    public void setTableendtime(LocalTime tableendtime) {this.tableendtime = tableendtime;}

    public String getLocation(){return location;}
    public void setLocation(String location){this.location = location;}

    public String getCustomer(){return customer;}
    public void setCustomer(String customer){this.customer = customer;}

    public String getCustomerPhone(){return customerPhone;}
    public void setCustomerPhone(String customerPhone){this.customerPhone = customerPhone;}

    public String getCustomerEmail(){return customerEmail;}
    public void setCustomerEmail(String customerEmail){this.customerEmail = customerEmail;}

    public String getReference(){return reference;}
    public void setReference(String reference){this.reference = reference;}

    public Integer getPax(){return pax;}
    public void setPax(Integer pax){this.pax = pax;}

    public List<Reservation> getReservations(){return reservations;}
    public void setReservations(List<Reservation> reservations){this.reservations = reservations;}

    @Override
    public String toString() {
        return tableNo;
    }

}
