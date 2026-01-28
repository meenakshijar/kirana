package com.example.kirana.dao;

import com.example.kirana.model.TransactionType;
import com.example.kirana.model.postgres.PurchaseLineItems;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface PurchaseLineItemsDao {

    List<PurchaseLineItems> saveAll(List<PurchaseLineItems> items);

    List<PurchaseLineItems> findByPurchaseId(String purchaseId);




    BigDecimal sumTotalByStoreAndTypeAndCreatedAtBetween(
            String storeId,
            TransactionType transactionType,
            LocalDateTime start,
            LocalDateTime end
    );
}
