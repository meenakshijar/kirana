package com.example.kirana.model.mongo;

import com.example.kirana.model.TransactionType;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Setter
@Getter
@Document(collection = "transactions") // this is your MongoDB VIEW name
public class Transactions {

    @Id
    private String transactionId;

    private String storeId;
    private TransactionType transactionType;
    private BigDecimal totalAmount;
    private String baseCurrency;
    private LocalDateTime createdAt;



}
