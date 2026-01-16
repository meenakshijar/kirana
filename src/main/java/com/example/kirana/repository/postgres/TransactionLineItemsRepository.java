package com.example.kirana.repository.postgres;

import com.example.kirana.model.TransactionType;
import com.example.kirana.model.postgres.TransactionLineItems;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransactionLineItemsRepository
        extends JpaRepository<TransactionLineItems, String> {

    // 1️⃣ Fetch all line items of a transaction
    List<TransactionLineItems> findByTransactionId(String transactionId);

    // 2️⃣ Fetch store transactions for weekly/monthly/yearly report (date range)
    List<TransactionLineItems> findByStoreIdAndCreatedAtBetween(
            String storeId,
            LocalDateTime start,
            LocalDateTime end
    );

    // 3️⃣ Sum totalProductAmount for CREDIT or DEBIT (for netflow)
    @Query("""
        SELECT COALESCE(SUM(t.totalProductAmount), 0)
        FROM TransactionLineItems t
        WHERE t.storeId = :storeId
          AND t.transactionType = :transactionType
          AND t.createdAt BETWEEN :start AND :end
    """)
    BigDecimal sumTotalByStoreAndTypeAndCreatedAtBetween(
            String storeId,
            TransactionType transactionType,
            LocalDateTime start,
            LocalDateTime end
    );
}
