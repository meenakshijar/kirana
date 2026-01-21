package com.example.kirana.dto;


import lombok.Data;

import java.math.BigDecimal;
@Data
public class PurchaseItemsRequest{

    private String productId;
    private Integer quantity;
    private BigDecimal amount;
    private String currency;



    // getters + setters
}
