package com.mycompany.reservationsystem.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record SalesReportsDTO(
        LocalDate date,
        long totalReservations,
        long totalPax,
        BigDecimal totalSales
) {}
