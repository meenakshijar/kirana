package com.example.kirana.dto;

import java.math.BigDecimal;
import lombok.Data;

 @Data
public class TransactionItemsRequest {

    private String productId;
    private Integer quantity;
    private BigDecimal amount;
    private String currency;


}
