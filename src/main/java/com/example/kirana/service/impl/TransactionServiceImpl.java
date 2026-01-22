package com.example.kirana.service.impl;

import com.example.kirana.dao.TransactionLineItemsDao;
import com.example.kirana.dto.TransactionDetailItemsRequest;
import com.example.kirana.dto.TransactionDetailItemsResponse;
import com.example.kirana.dto.TransactionItemsRequest;
import com.example.kirana.dto.TransactionRequest;
import com.example.kirana.dto.TransactionResponse;
import com.example.kirana.dto.TransactionSummary;
import com.example.kirana.lock.RedissonLockService;
import com.example.kirana.model.TransactionType;
import com.example.kirana.model.mongo.Store;
import com.example.kirana.model.mongo.Transactions;
import com.example.kirana.model.postgres.TransactionLineItems;
import com.example.kirana.repository.mongo.ProductsRepository;
import com.example.kirana.repository.mongo.StoreRepository;
import com.example.kirana.repository.mongo.TransactionsRepository;
import com.example.kirana.service.FxRateService;
import com.example.kirana.service.TransactionService;
import org.redisson.api.RLock;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class TransactionServiceImpl implements TransactionService {

    private final TransactionLineItemsDao transactionLineItemsDao;
    private final TransactionsRepository transactionsRepository;
    private final FxRateService fxRateService;
    private final ProductsRepository productsRepository;
    private final StoreRepository storeRepository;
    private final RedissonLockService redissonLockService;

    public TransactionServiceImpl(TransactionLineItemsDao transactionLineItemsDao,
                                  TransactionsRepository transactionsRepository,
                                  FxRateService fxRateService,
                                  ProductsRepository productsRepository,
                                  StoreRepository storeRepository,
                                  RedissonLockService redissonLockService) {
        this.transactionLineItemsDao = transactionLineItemsDao;
        this.transactionsRepository = transactionsRepository;
        this.fxRateService = fxRateService;
        this.productsRepository = productsRepository;
        this.storeRepository = storeRepository;
        this.redissonLockService = redissonLockService;
    }

    @Transactional
    @Override
    public TransactionResponse createTransaction(TransactionRequest request) {

        // Basic request validations
        if (request.getStoreId() == null || request.getStoreId().isBlank()) {
            throw new RuntimeException("storeId is required");
        }

        if (request.getTransactionType() == null || request.getTransactionType().isBlank()) {
            throw new RuntimeException("transactionType is required");
        }

        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new RuntimeException("Transaction items are required");
        }

        // Fetch store ONCE
        Store store = storeRepository.findById(request.getStoreId())
                .orElseThrow(() -> new RuntimeException("Store not found: " + request.getStoreId()));

        String baseCurrency = store.getBaseCurrency();
        if (baseCurrency == null || baseCurrency.isBlank()) {
            throw new RuntimeException("Base currency not configured for store: " + request.getStoreId());
        }

        // Lock per store
        String lockKey = "lock:transaction:store:" + request.getStoreId();
        RLock lock = redissonLockService.lock(lockKey, 2, 15);

        if (lock == null) {
            throw new RuntimeException("Another transaction is already processing for this store. Try again.");
        }

        try {
            String transactionId = "TXN_" + UUID.randomUUID();
            TransactionType type = TransactionType.valueOf(request.getTransactionType().toUpperCase());

            // Validate each item
            for (TransactionItemsRequest item : request.getItems()) {

                if (item.getProductId() == null || item.getProductId().isBlank()) {
                    throw new RuntimeException("productId is required");
                }

                if (item.getQuantity() == null || item.getQuantity() <= 0) {
                    throw new RuntimeException("Invalid quantity for product: " + item.getProductId());
                }

                if (item.getAmount() == null || item.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
                    throw new RuntimeException("Invalid amount for product: " + item.getProductId());
                }

                if (item.getCurrency() == null || item.getCurrency().isBlank()) {
                    throw new RuntimeException("Currency is required for product: " + item.getProductId());
                }

                // Validate product exists + belongs to store
                var product = productsRepository.findById(item.getProductId())
                        .orElseThrow(() -> new RuntimeException("Product not found: " + item.getProductId()));

                if (!request.getStoreId().equals(product.getStoreId())) {
                    throw new RuntimeException(
                            "Product " + item.getProductId() + " does not belong to store " + request.getStoreId()
                    );
                }
            }

            // Prepare Postgres ledger items
            List<TransactionLineItems> itemsToSave = new ArrayList<>();

            for (TransactionItemsRequest item : request.getItems()) {

                BigDecimal originalTotal = item.getAmount()
                        .multiply(BigDecimal.valueOf(item.getQuantity()));

                BigDecimal rate = fxRateService.getFxRate(item.getCurrency(), baseCurrency);

                BigDecimal baseTotal = originalTotal.multiply(rate);

                TransactionLineItems lineItems = new TransactionLineItems();
                lineItems.setTransactionItemId("TXN_ITEM_" + UUID.randomUUID());
                lineItems.setTransactionId(transactionId);

                lineItems.setStoreId(request.getStoreId());
                lineItems.setTransactionType(type);

                lineItems.setProductId(item.getProductId());
                lineItems.setQuantity(item.getQuantity());

                lineItems.setPricePerItem(item.getAmount());
                lineItems.setOriginalCurrency(item.getCurrency());

                lineItems.setBaseCurrency(baseCurrency);
                lineItems.setConversionRate(rate);

                lineItems.setTotalProductAmount(baseTotal);
                lineItems.setCreatedAt(LocalDateTime.now());

                itemsToSave.add(lineItems);
            }

            // Save Postgres
            transactionLineItemsDao.saveAll(itemsToSave);

            // Calculate total
            BigDecimal txnTotal = itemsToSave.stream()
                    .map(TransactionLineItems::getTotalProductAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // Sync Mongo view (transactions collection)
            Transactions txn = new Transactions();
            txn.setTransactionId(transactionId);
            txn.setStoreId(request.getStoreId());
            txn.setTransactionType(type);
            txn.setTotalAmount(txnTotal);
            txn.setBaseCurrency(baseCurrency);
            txn.setCreatedAt(LocalDateTime.now());

            transactionsRepository.save(txn);

            // Response
            TransactionResponse response = new TransactionResponse();
            response.setTransactionId(transactionId);
            response.setStoreId(request.getStoreId());
            response.setMessage("Transaction recorded successfully");

            return response;

        } finally {
            redissonLockService.unlock(lock);
        }
    }

    @Override
    public List<TransactionSummary> getTransactionsByStoreId(String storeId) {

        List<Transactions> transactions = transactionsRepository.findByStoreId(storeId);

        List<TransactionSummary> result = new ArrayList<>();

        for (Transactions view : transactions) {
            TransactionSummary summary = new TransactionSummary();
            summary.setTransactionId(view.getTransactionId());
            summary.setTransactionType(view.getTransactionType().name());
            summary.setTotalAmount(view.getTotalAmount());
            summary.setBaseCurrency(view.getBaseCurrency());
            summary.setCreatedAt(view.getCreatedAt());
            result.add(summary);
        }

        return result;
    }

    @Override
    public TransactionDetailItemsResponse getTransactionByTransactionId(String transactionId) {

        List<TransactionLineItems> dbItems = transactionLineItemsDao.findByTransactionId(transactionId);

        if (dbItems.isEmpty()) {
            throw new RuntimeException("Transaction not found: " + transactionId);
        }

        String storeId = dbItems.get(0).getStoreId();
        String transactionType = dbItems.get(0).getTransactionType().name();
        String baseCurrency = dbItems.get(0).getBaseCurrency();

        BigDecimal totalAmount = BigDecimal.ZERO;
        List<TransactionDetailItemsRequest> items = new ArrayList<>();

        for (TransactionLineItems li : dbItems) {

            TransactionDetailItemsRequest detailItems = new TransactionDetailItemsRequest();
            detailItems.setProductId(li.getProductId());
            detailItems.setQuantity(li.getQuantity());
            detailItems.setOriginalAmount(li.getPricePerItem());
            detailItems.setOriginalCurrency(li.getOriginalCurrency());
            detailItems.setExchangeRateUsed(li.getConversionRate());
            detailItems.setBaseAmount(li.getTotalProductAmount());

            totalAmount = totalAmount.add(li.getTotalProductAmount());
            items.add(detailItems);
        }

        TransactionDetailItemsResponse response = new TransactionDetailItemsResponse();
        response.setTransactionId(transactionId);
        response.setStoreId(storeId);
        response.setTransactionType(transactionType);
        response.setBaseCurrency(baseCurrency);
        response.setItems(items);
        response.setTotalAmount(totalAmount);

        return response;
    }
}
