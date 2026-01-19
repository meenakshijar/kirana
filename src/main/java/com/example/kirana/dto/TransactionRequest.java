package com.example.kirana.dto;

import lombok.Data;


import java.util.List;
@Data
public class TransactionRequest {

    private String storeId;
    private String transactionType; // CREDIT / DEBIT
    private List<com.example.kirana.dto.TransactionItemsRequest> items;

    // getters + setters
}
