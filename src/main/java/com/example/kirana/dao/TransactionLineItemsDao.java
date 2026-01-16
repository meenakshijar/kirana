package com.example.kirana.dao;

import com.example.kirana.model.TransactionType;
import com.example.kirana.model.postgres.TransactionLineItems;


import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface TransactionLineItemsDao {

    TransactionLineItems save(TransactionLineItems item);

    List<TransactionLineItems> saveAll(List<TransactionLineItems> items);

    List<TransactionLineItems> findByTransactionId(String transactionId);

    List<TransactionLineItems> findByStoreIdAndCreatedAtBetween(
            String storeId,
            LocalDateTime start,
            LocalDateTime end
    );

    BigDecimal sumTotalByStoreAndTypeAndCreatedAtBetween(
            String storeId,
            TransactionType transactionType,
            LocalDateTime start,
            LocalDateTime end
    );
}
