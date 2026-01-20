package com.example.kirana.dto;

import lombok.Data;

@Data
public class StoreResponse {
    private String storeId;
    private String storeName;
    private String city;
    private String country;
    private String baseCurrency;
    private String message;
}
