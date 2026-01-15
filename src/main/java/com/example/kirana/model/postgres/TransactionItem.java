package com.example.kirana.model.postgres;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
    @Table(name = "transaction_items")
    public class TransactionItem {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @Column(name = "transaction_id", nullable = false)
        private String transactionId;

        @Column(name = "store_id", nullable = false)
        private String storeId;

        @Column(name = "transaction_type", nullable = false)
        private String transactionType; // CREDIT / DEBIT

        @Column(name = "product_id", nullable = false)
        private String productId;

        @Column(nullable = false)
        private Integer quantity;

        @Column(name = "original_amount", nullable = false)
        private Double originalAmount;

        @Column(name = "original_currency", nullable = false)
        private String originalCurrency;

        @Column(name = "base_currency", nullable = false)
        private String baseCurrency;

        @Column(name = "exchange_rate_used", nullable = false)
        private Double exchangeRateUsed;

        @Column(name = "base_amount", nullable = false)
        private Double baseAmount;

        @Column(name = "created_at", updatable = false)
        private LocalDateTime createdAt = LocalDateTime.now();

        // getters & setters
    }


