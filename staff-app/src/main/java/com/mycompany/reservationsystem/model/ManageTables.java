/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.reservationsystem.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.sql.Time;
import java.time.LocalTime;
import java.util.List;

/**
 *
 * @author formentera
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ManageTables {
    
    Long id;
    String tableNo;
    int capacity;
    String status;
    String location;
    LocalTime tablestarttime;
    LocalTime tableendtime;
    String customer;
    String customerPhone;
    String customerEmail;
    String reference;
    Integer pax;
    
    @JsonProperty("id")
    public Long getId(){return id;}
    public void setId(Long id){this.id = id;}
    
    @JsonProperty("tableNo")
    public String getTableNo(){return tableNo;}
    public void setTableNo(String tableNo){this.tableNo = tableNo;}
    
    @JsonProperty("status")
    public String getStatus(){return status;}
    public void setStatus(String Status){this.status = Status;}
    
    @JsonProperty("capacity")
    public int getCapacity(){return capacity;}
    public void setCapacity(int Capacity){this.capacity = Capacity;}
    
    @JsonProperty("tablestarttime")
    public LocalTime getTablestarttime() {return tablestarttime;}
    public void setTablestarttime(LocalTime tablestarttime) {this.tablestarttime = tablestarttime;}

    @JsonProperty("tableendtime")
    public LocalTime getTableendtime() {return tableendtime;}
    public void setTableendtime(LocalTime tableendtime) {this.tableendtime = tableendtime;}

    @JsonProperty("location")
    public String getLocation(){return location;}
    public void setLocation(String location){this.location = location;}

    @JsonProperty("customer")
    public String getCustomer(){return customer;}
    public void setCustomer(String customer){this.customer = customer;}

    @JsonProperty("customerPhone")
    public String getCustomerPhone(){return customerPhone;}
    public void setCustomerPhone(String customerPhone){this.customerPhone = customerPhone;}

    @JsonProperty("customerEmail")
    public String getCustomerEmail(){return customerEmail;}
    public void setCustomerEmail(String customerEmail){this.customerEmail = customerEmail;}

    @JsonProperty("reference")
    public String getReference(){return reference;}
    public void setReference(String reference){this.reference = reference;}

    @JsonProperty("pax")
    public Integer getPax(){return pax;}
    public void setPax(Integer pax){this.pax = pax;}

    @Override
    public String toString() {
        return tableNo;
    }

}


