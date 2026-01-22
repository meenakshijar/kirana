package com.example.kirana.dto;


import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
@Data
public class TransactionSummary {
    @NotBlank(message = "transactionId is required")
    private String transactionId;
    @NotBlank(message = "transactionType is required")
    private String transactionType;
    @NotBlank(message = "totalAmount is required")
    private BigDecimal totalAmount;
    @NotBlank(message = "baseCurrency is required")
    private String baseCurrency;
    @NotBlank(message = "createdAt is required")
    private LocalDateTime createdAt;


}
