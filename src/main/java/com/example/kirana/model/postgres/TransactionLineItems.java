package com.example.kirana.model.postgres;

import com.example.kirana.model.TransactionType;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "transactionLineItems")
public class TransactionLineItems {

    @Id
    private String transactionItemId;

    private String transactionId;
    private String storeId;

    @Enumerated(EnumType.STRING)
    private TransactionType transactionType;

    private String productId;
    private Integer quantity;

    @Column(nullable = false)
    private BigDecimal pricePerItem;

    private String originalCurrency;
    private String baseCurrency;

    private BigDecimal conversionRate;

    @Column(nullable = false)
    private BigDecimal totalProductAmount;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
