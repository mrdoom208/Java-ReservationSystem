package com.mycompany.reservationsystem.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

public class TableUsageInformationDTO {
    private String tableNo;
    private String reference;
    private Integer pax;
    private BigDecimal sales;
    private LocalTime time;
    private LocalDate date;

    public TableUsageInformationDTO(String tableNo, String reference, Integer pax,
                                    BigDecimal sales, LocalTime time, LocalDate date) {
        this.tableNo = tableNo;
        this.reference = reference;
        this.pax = pax;
        this.sales = sales;
        this.time = time;
        this.date = date;
    }

    public String getTableNo() {
        return tableNo;
    }

    public String getReference() {
        return reference;
    }

    public Integer getPax() {
        return pax;
    }

    public BigDecimal getSales() {
        return sales;
    }

    public LocalDate getDate() {
        return date;
    }

    public LocalTime getTime() {
        return time;
    }
}