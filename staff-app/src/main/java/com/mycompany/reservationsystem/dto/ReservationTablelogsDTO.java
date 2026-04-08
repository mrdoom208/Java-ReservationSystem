package com.mycompany.reservationsystem.dto;

import java.util.List;

public class ReservationTablelogsDTO {
    private Long reservationId;
    private String reference;
    private String customerName;
    private String tableNumber;
    private String oldTableNumber;
    private String status;
    private String timestamp;
    private List<ReservationTablelogsDTO> logs;
    private int CompleteCount;

    public ReservationTablelogsDTO() {}

    public ReservationTablelogsDTO(Long reservationId, String reference, String customerName, String tableNumber, String status) {
        this.reservationId = reservationId;
        this.reference = reference;
        this.customerName = customerName;
        this.tableNumber = tableNumber;
        this.status = status;
    }

    public Long getReservationId() { return reservationId; }
    public void setReservationId(Long reservationId) { this.reservationId = reservationId; }

    public String getReference() { return reference; }
    public void setReference(String reference) { this.reference = reference; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getTableNumber() { return tableNumber; }
    public void setTableNumber(String tableNumber) { this.tableNumber = tableNumber; }

    public String getOldTableNumber() { return oldTableNumber; }
    public void setOldTableNumber(String oldTableNumber) { this.oldTableNumber = oldTableNumber; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }

    public List<ReservationTablelogsDTO> getLogs() { return logs; }
    public void setLogs(List<ReservationTablelogsDTO> logs) { this.logs = logs; }

    public int getCompleteCount() { return CompleteCount; }
    public void setCompleteCount(int CompleteCount) { this.CompleteCount = CompleteCount; }
}
