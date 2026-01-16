package com.example.kirana.model.postgres;

import com.example.kirana.model.TransactionType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
@Data
@Entity
@Table(name = "purchaseLineItems")
public class PurchaseLineItems {

    @Id
    private String purchaseItemId;

    private String purchaseId;

    private String storeId;

    @Enumerated(EnumType.STRING)
    private TransactionType transactionType; // DEBIT / CREDIT

    private String productId;

    private Integer quantity;

    @Column(nullable = false)
    private BigDecimal pricePerItem;

    private String originalCurrency;

    private String baseCurrency;

    private BigDecimal conversionRate;

    @Column(nullable = false)
    private BigDecimal totalAmount;



    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
