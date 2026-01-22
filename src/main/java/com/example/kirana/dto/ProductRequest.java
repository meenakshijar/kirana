package com.example.kirana.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductRequest {

    @NotBlank(message = "store id is required")
    private String storeId;

    @NotBlank(message = "productName is required")
    private String productName;

    @NotNull(message = "cost price is required")
    @PositiveOrZero(message = "cost price must be >= 0")
    private BigDecimal costPrice;

    @NotNull(message = "sellingPrice is required")
    @PositiveOrZero(message = "sellingPrice must be >= 0")
    private BigDecimal sellingPrice;

    @NotNull(message = "mrp is required")
    @PositiveOrZero(message = "mrp must be >= 0")
    private BigDecimal mrp;

    @NotNull(message = "discountPercentage is required")
    @DecimalMin(value = "0.0", message = "discountPercentage must be >= 0")
    @DecimalMax(value = "100.0", message = "discountPercentage must be <= 100")
    private BigDecimal discountPercentage;

    @NotBlank(message = "unit is required")
    private String unit;

    @NotBlank(message = "brand is required")
    private String brand;

    @NotBlank(message = "category is required")
    private String category;
}
