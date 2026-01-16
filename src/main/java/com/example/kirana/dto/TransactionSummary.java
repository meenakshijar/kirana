package com.example.kirana.dto;


import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
@Data
public class TransactionSummary {

    private String transactionId;
    private String transactionType;
    private BigDecimal totalAmount;
    private String baseCurrency;
    private LocalDateTime createdAt;

    // getters + setters
}
