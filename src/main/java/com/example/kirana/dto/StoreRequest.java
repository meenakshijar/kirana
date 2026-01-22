package com.example.kirana.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class StoreRequest {

    @NotBlank(message = "storeName is required")

    private String storeName;

    @NotBlank(message = "city is required")

    private String city;

    @NotBlank(message = "country is required")

    private String country;

    @NotBlank(message = "address is required")

    private String address;

    @NotBlank(message = "pincode is required")
    private String pincode;

    @NotBlank(message = "ContactEmail is required")
    private String contactEmail;

    @NotBlank(message = "contactNumber is required")

    private String contactNumber;

    @NotBlank(message = "baseCurrency is required")

    private String baseCurrency;
}
