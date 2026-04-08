package com.mycompany.reservationsystem.dto;

public class TableUsageInformationDTO {
    private Long tableId;
    private String tableNumber;
    private String date;
    private int reservationCount;
    private double sales;
    private String tableNo;
    private String reference;
    private int pax;
    private String time;

    public TableUsageInformationDTO() {}

    public TableUsageInformationDTO(Long tableId, String tableNumber, String date, int reservationCount, double sales) {
        this.tableId = tableId;
        this.tableNumber = tableNumber;
        this.date = date;
        this.reservationCount = reservationCount;
        this.sales = sales;
    }

    public Long getTableId() { return tableId; }
    public void setTableId(Long tableId) { this.tableId = tableId; }

    public String getTableNumber() { return tableNumber; }
    public void setTableNumber(String tableNumber) { this.tableNumber = tableNumber; }

    public String getTableNo() { return tableNo != null ? tableNo : tableNumber; }
    public void setTableNo(String tableNo) { this.tableNo = tableNo; }

    public String getReference() { return reference; }
    public void setReference(String reference) { this.reference = reference; }

    public int getPax() { return pax; }
    public void setPax(int pax) { this.pax = pax; }

    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public int getReservationCount() { return reservationCount; }
    public void setReservationCount(int reservationCount) { this.reservationCount = reservationCount; }

    public double getSales() { return sales; }
    public void setSales(double sales) { this.sales = sales; }
}
