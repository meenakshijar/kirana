package com.example.kirana.dto;


import lombok.Data;

import java.math.BigDecimal;
@Data
public class PurchaseItemsRequest{

    private String productId;
    private Integer quantity;
    private BigDecimal amount;
    private String currency;

    // optional fields
    private String supplierName;
    private String invoiceNumber;

    // getters + setters
}
