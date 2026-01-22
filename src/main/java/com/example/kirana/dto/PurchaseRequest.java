package com.example.kirana.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;
@Data
public class PurchaseRequest {

    @NotBlank(message = "store id is required")

    private String storeId;

    @NotBlank(message = "Transaction type is required")

    private String transactionType;

    @Valid
    @NotEmpty(message = "items cannot be empty")

    private List<PurchaseItemsRequest> items;


}
