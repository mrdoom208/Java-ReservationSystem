package com.mycompany.reservationsystem.dto;

public class TableUsageReportDTO {
    private String tableNumber;
    private int totalReservations;
    private double totalSales;
    private double averageSales;
    private int totalCustomer;

    public TableUsageReportDTO() {}

    public TableUsageReportDTO(String tableNumber, int totalReservations, double totalSales, double averageSales) {
        this.tableNumber = tableNumber;
        this.totalReservations = totalReservations;
        this.totalSales = totalSales;
        this.averageSales = averageSales;
    }

    public String getTableNumber() { return tableNumber; }
    public void setTableNumber(String tableNumber) { this.tableNumber = tableNumber; }

    public String getTableNo() { return tableNumber; }
    public void setTableNo(String tableNo) { this.tableNumber = tableNo; }

    public int getTotalReservations() { return totalReservations; }
    public void setTotalReservations(int totalReservations) { this.totalReservations = totalReservations; }

    public double getTotalSales() { return totalSales; }
    public void setTotalSales(double totalSales) { this.totalSales = totalSales; }

    public double getAverageSales() { return averageSales; }
    public void setAverageSales(double averageSales) { this.averageSales = averageSales; }

    public int getTotalCustomer() { return totalCustomer; }
    public void setTotalCustomer(int totalCustomer) { this.totalCustomer = totalCustomer; }

    public int getTotalReservation() { return totalReservations; }
}
