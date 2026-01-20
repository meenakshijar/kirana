package com.example.kirana.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductRequest {

    private String storeId;

    private String productName;

    private BigDecimal costPrice;
    private BigDecimal sellingPrice;
    private BigDecimal mrp;
    private BigDecimal discountPercentage;

    private String unit;
    private String brand;
    private String category;
}
