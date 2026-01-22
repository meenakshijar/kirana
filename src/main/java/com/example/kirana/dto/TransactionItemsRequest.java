package com.example.kirana.dto;

import java.math.BigDecimal;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

 @Data
public class TransactionItemsRequest {

     @NotBlank(message = "productId is required")
     private String productId;

     @NotNull(message = "quantity is required")
     @Positive(message = "quantity must be > 0")
     private Integer quantity;

     @NotNull(message = "amount is required")
     private BigDecimal amount;

     @NotBlank(message = "currency is required")
     private String currency;


}
