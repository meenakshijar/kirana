package com.example.kirana.dto;

import lombok.Data;


import java.util.List;
@Data
public class TransactionRequest {

    private String storeId;
    private String transactionType; // CREDIT / DEBIT
    private List<TransactionItemsRequest> items;

    // getters + setters
}
