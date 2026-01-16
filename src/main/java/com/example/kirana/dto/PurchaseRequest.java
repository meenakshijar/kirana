package com.example.kirana.dto;

import lombok.Data;

import java.util.List;
@Data
public class PurchaseRequest {

    private String storeId;
    private String transactionType; // DEBIT / CREDIT (mostly DEBIT)
    private List<PurchaseItemsRequest> items;

    // getters + setters
}
