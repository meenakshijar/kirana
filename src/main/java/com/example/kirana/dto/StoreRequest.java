package com.example.kirana.dto;

import lombok.Data;

@Data
public class StoreRequest {
    private String storeName;
    private String city;
    private String country;
    private String address;
    private String pincode;
    private String contactEmail;
    private String contactNumber;
    private String baseCurrency;
}
