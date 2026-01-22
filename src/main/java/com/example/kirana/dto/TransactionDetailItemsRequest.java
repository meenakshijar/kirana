package com.example.kirana.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;
@Data
public class TransactionDetailItemsRequest {

    @NotBlank(message = "productId is required")
    private String productId;
    @NotBlank(message = "quantity is required")
    private Integer quantity;
    @NotBlank(message = "originalAmount is required")
    private BigDecimal originalAmount;
    @NotBlank(message = "originalCurrency is required")
    private String originalCurrency;
    @NotBlank(message = "exchangeRateUsed is required")
    private BigDecimal exchangeRateUsed;
    @NotBlank(message = "baseAmount is required")
    private BigDecimal baseAmount;

}
