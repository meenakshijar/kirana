package com.example.kirana.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
@Data
public class TransactionDetailItemsResponse {

    private String transactionId;
    private String storeId;
    private String transactionType;
    private String baseCurrency;

    private List<TransactionDetailItemsRequest> items;

    private BigDecimal totalAmount;

    // getters + setters
}
