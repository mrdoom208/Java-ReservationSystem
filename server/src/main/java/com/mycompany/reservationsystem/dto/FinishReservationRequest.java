package com.mycompany.reservationsystem.dto;

import java.math.BigDecimal;

public class FinishReservationRequest {
    private BigDecimal amountPaid;
    private BigDecimal totalAmount;

    public BigDecimal getAmountPaid() {
        return amountPaid;
    }

    public void setAmountPaid(BigDecimal amountPaid) {
        this.amountPaid = amountPaid;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }
}
