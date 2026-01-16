package com.example.kirana.dto;

import lombok.Data;

@Data
public class TransactionResponse {

    private String transactionId;
    private String storeId;
    private String message;

    // getters + setters
}
