package com.example.kirana.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;


import java.math.BigDecimal;
import java.util.List;
@Data
public class TransactionRequest {
    @NotNull(message = "storeId is required")
    private String storeId;

    @NotNull(message = "transactionType is required")
    private String transactionType;

    @Valid
    @NotEmpty(message = "items cannot be empty")
    private List<TransactionItemsRequest> items;



    // getters + setters
}
