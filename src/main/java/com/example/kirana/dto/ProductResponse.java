package com.example.kirana.dto;

import lombok.Data;

@Data
public class ProductResponse {

    private String productId;
    private String storeId;

    private String productName;
    private String category;

    private String message;
}
