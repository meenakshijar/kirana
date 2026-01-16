package com.example.kirana.dto;

import lombok.Data;

import java.math.BigDecimal;
@Data
public class TransactionDetailItemsRequest {
    private String productId;
    private Integer quantity;
    private BigDecimal originalAmount;
    private String originalCurrency;
    private BigDecimal exchangeRateUsed;
    private BigDecimal baseAmount;

}
