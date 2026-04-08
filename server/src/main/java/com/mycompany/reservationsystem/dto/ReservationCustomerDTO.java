package com.mycompany.reservationsystem.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

public class ReservationCustomerDTO {

    // Reservation fields
    private Long reservationId;
    private int pax;
    private String prefer;
    private String status;
    private String reference;
    private LocalDate date;
    private BigDecimal sales;
    private LocalTime reservationPendingtime;
    private LocalTime reservationConfirmtime;
    private LocalTime reservationCancelledtime;
    private LocalTime reservationSeatedtime;
    private LocalTime reservationCompletetime;

    // Customer fields
    private Long customerId;
    private String customerName;
    private String customerPhone;
    private String customerEmail;

    // Constructor
    public ReservationCustomerDTO(Long reservationId, int pax, String prefer, String status, String reference,
                                  LocalDate date, BigDecimal sales, LocalTime reservationPendingtime,
                                  LocalTime reservationConfirmtime, LocalTime reservationCancelledtime,
                                  LocalTime reservationSeatedtime, LocalTime reservationCompletetime,
                                  Long customerId, String customerName, String customerPhone, String customerEmail) {
        this.reservationId = reservationId;
        this.pax = pax;
        this.prefer = prefer;
        this.status = status;
        this.reference = reference;
        this.date = date;
        this.sales = sales;
        this.reservationPendingtime = reservationPendingtime;
        this.reservationConfirmtime = reservationConfirmtime;
        this.reservationCancelledtime = reservationCancelledtime;
        this.reservationSeatedtime = reservationSeatedtime;
        this.reservationCompletetime = reservationCompletetime;

        this.customerId = customerId;
        this.customerName = customerName;
        this.customerPhone = customerPhone;
        this.customerEmail = customerEmail;
    }

    // ============================
    //         Getters
    // ============================

    public Long getReservationId() { return reservationId; }
    public int getPax() { return pax; }
    public String getPrefer() { return prefer; }
    public String getStatus() { return status; }
    public String getReference() { return reference; }
    public LocalDate getDate() { return date; }
    public BigDecimal getSales() { return sales; }
    public LocalTime getReservationPendingtime() { return reservationPendingtime; }
    public LocalTime getReservationConfirmtime() { return reservationConfirmtime; }
    public LocalTime getReservationCancelledtime() { return reservationCancelledtime; }
    public LocalTime getReservationSeatedtime() { return reservationSeatedtime; }
    public LocalTime getReservationCompletetime() { return reservationCompletetime; }

    public Long getCustomerId() { return customerId; }
    public String getCustomerName() { return customerName; }
    public String getCustomerPhone() { return customerPhone; }
    public String getCustomerEmail() { return customerEmail; }
}
