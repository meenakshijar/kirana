package com.example.kirana.dao.impl;


import com.example.kirana.dao.TransactionLineItemsDao;
import com.example.kirana.model.TransactionType;
import com.example.kirana.model.postgres.TransactionLineItems;
import com.example.kirana.repository.postgres.TransactionLineItemsRepository;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class TransactionLineItemsDaoImpl implements TransactionLineItemsDao {

    private final TransactionLineItemsRepository transactionLineItemsRepository;

    public TransactionLineItemsDaoImpl(TransactionLineItemsRepository transactionLineItemRepository) {
        this.transactionLineItemsRepository = transactionLineItemRepository;
    }

    @Override
    public TransactionLineItems save(TransactionLineItems item) {
        return transactionLineItemsRepository.save(item);
    }

    @Override
    public List<TransactionLineItems> saveAll(List<TransactionLineItems> items) {
        return transactionLineItemsRepository.saveAll(items);
    }

    @Override
    public List<TransactionLineItems> findByTransactionId(String transactionId) {
        return transactionLineItemsRepository.findByTransactionId(transactionId);
    }

    @Override
    public List<TransactionLineItems> findByStoreIdAndCreatedAtBetween(
            String storeId,
            LocalDateTime start,
            LocalDateTime end
    ) {
        return transactionLineItemsRepository.findByStoreIdAndCreatedAtBetween(storeId, start, end);
    }

    @Override
    public BigDecimal sumTotalByStoreAndTypeAndCreatedAtBetween(
            String storeId,
            TransactionType transactionType,
            LocalDateTime start,
            LocalDateTime end
    ) {
        return transactionLineItemsRepository.sumTotalByStoreAndTypeAndCreatedAtBetween(
                storeId, transactionType, start, end
        );
    }
}
