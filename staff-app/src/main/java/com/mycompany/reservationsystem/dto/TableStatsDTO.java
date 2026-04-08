package com.mycompany.reservationsystem.dto;

public class TableStatsDTO {
    private Long tableId;
    private String tableNumber;
    private int totalReservations;
    private double totalSales;
    private double averageSales;
    private int tables;
    private int total;
    private int free;
    private int busy;

    public TableStatsDTO() {}

    public TableStatsDTO(Long tableId, String tableNumber, int totalReservations, double totalSales, double averageSales) {
        this.tableId = tableId;
        this.tableNumber = tableNumber;
        this.totalReservations = totalReservations;
        this.totalSales = totalSales;
        this.averageSales = averageSales;
    }

    public Long getTableId() { return tableId; }
    public void setTableId(Long tableId) { this.tableId = tableId; }

    public String getTableNumber() { return tableNumber; }
    public void setTableNumber(String tableNumber) { this.tableNumber = tableNumber; }

    public int getTotalReservations() { return totalReservations; }
    public void setTotalReservations(int totalReservations) { this.totalReservations = totalReservations; }

    public double getTotalSales() { return totalSales; }
    public void setTotalSales(double totalSales) { this.totalSales = totalSales; }

    public double getAverageSales() { return averageSales; }
    public void setAverageSales(double averageSales) { this.averageSales = averageSales; }

    public int getTables() { return tables; }
    public void setTables(int tables) { this.tables = tables; }

    public int getTotal() { return total; }
    public void setTotal(int total) { this.total = total; }

    public int getFree() { return free; }
    public void setFree(int free) { this.free = free; }

    public int getBusy() { return busy; }
    public void setBusy(int busy) { this.busy = busy; }
}
