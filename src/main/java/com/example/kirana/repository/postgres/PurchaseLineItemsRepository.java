package com.example.kirana.repository.postgres;

import com.example.kirana.model.postgres.PurchaseLineItems;
import com.example.kirana.model.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PurchaseLineItemsRepository
        extends JpaRepository<PurchaseLineItems, String> {

    // 1️⃣ Fetch all line items of a purchase
    List<PurchaseLineItems> findByPurchaseId(String purchaseId);

    // 2️⃣ Fetch store purchases for weekly/monthly/yearly report (date range)
    List<PurchaseLineItems> findByStoreIdAndCreatedAtBetween(
            String storeId,
            LocalDateTime start,
            LocalDateTime end
    );

    // 3️⃣ Sum totalAmount for DEBIT or CREDIT (for report calculations)
    @Query("""
        SELECT COALESCE(SUM(p.totalAmount), 0)
        FROM PurchaseLineItems p
        WHERE p.storeId = :storeId
          AND p.transactionType = :transactionType
          AND p.createdAt BETWEEN :start AND :end
    """)
    BigDecimal sumTotalByStoreAndTypeAndCreatedAtBetween(
            String storeId,
            TransactionType transactionType,
            LocalDateTime start,
            LocalDateTime end
    );
}
