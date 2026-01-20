package com.example.kirana.model.mongo;


import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;
@Data
@Document(collection = "products")
public class Products {

    @Id
    private String productId;

    private String storeId;
    private String productName;
    private String category;

    private BigDecimal costPrice;
    private BigDecimal sellingPrice;
    private BigDecimal mrp;
    private BigDecimal discountPercentage;

    private String unit;
    private String brand;
    private LocalDateTime productCreatedAt;
}
