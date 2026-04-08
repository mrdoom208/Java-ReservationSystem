package com.mycompany.reservationsystem.dto;

public class DashboardCountsDTO {
    private long occupied;
    private long reserved;
    private long totalTables;
    private long todayCustomers;
    private long pending;
    private long cancelled;
    private long completed;

    public DashboardCountsDTO() {}

    public DashboardCountsDTO(long occupied, long reserved, long totalTables, long todayCustomers, long pending, long cancelled, long completed) {
        this.occupied = occupied;
        this.reserved = reserved;
        this.totalTables = totalTables;
        this.todayCustomers = todayCustomers;
        this.pending = pending;
        this.cancelled = cancelled;
        this.completed = completed;
    }

    public long occupied() { return occupied; }
    public long reserved() { return reserved; }
    public long totalTables() { return totalTables; }
    public long todayCustomers() { return todayCustomers; }
    public long pending() { return pending; }
    public long cancelled() { return cancelled; }
    public long completed() { return completed; }

    public void setOccupied(long occupied) { this.occupied = occupied; }
    public void setReserved(long reserved) { this.reserved = reserved; }
    public void setTotalTables(long totalTables) { this.totalTables = totalTables; }
    public void setTodayCustomers(long todayCustomers) { this.todayCustomers = todayCustomers; }
    public void setPending(long pending) { this.pending = pending; }
    public void setCancelled(long cancelled) { this.cancelled = cancelled; }
    public void setCompleted(long completed) { this.completed = completed; }
}
