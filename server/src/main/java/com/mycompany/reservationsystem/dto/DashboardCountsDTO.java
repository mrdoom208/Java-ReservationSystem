package com.mycompany.reservationsystem.dto;

public record DashboardCountsDTO(
        long occupied,
        long reserved,
        long totalTables,
        long totalCustomers,
        long pending,
        long cancelled
) {}