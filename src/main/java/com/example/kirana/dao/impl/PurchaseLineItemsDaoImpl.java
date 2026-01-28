package com.example.kirana.dao.impl;

import com.example.kirana.dao.PurchaseLineItemsDao;
import com.example.kirana.model.TransactionType;
import com.example.kirana.model.postgres.PurchaseLineItems;
import com.example.kirana.repository.postgres.PurchaseLineItemsRepository;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class PurchaseLineItemsDaoImpl implements PurchaseLineItemsDao {

    private final PurchaseLineItemsRepository purchaseLineItemsRepository;

    public PurchaseLineItemsDaoImpl(PurchaseLineItemsRepository purchaseLineItemsRepository) {
        this.purchaseLineItemsRepository = purchaseLineItemsRepository;
    }

    @Override
    public List<PurchaseLineItems> saveAll(List<PurchaseLineItems> items) {
        return purchaseLineItemsRepository.saveAll(items);
    }

    @Override
    public List<PurchaseLineItems> findByPurchaseId(String purchaseId) {
        return purchaseLineItemsRepository.findByPurchaseId(purchaseId);
    }


    @Override
    public BigDecimal sumTotalByStoreAndTypeAndCreatedAtBetween(
            String storeId,
            TransactionType transactionType,
            LocalDateTime start,
            LocalDateTime end
    ) {
        return purchaseLineItemsRepository.sumTotalByStoreAndTypeAndCreatedAtBetween(
                storeId, transactionType, start, end
        );
    }
}






