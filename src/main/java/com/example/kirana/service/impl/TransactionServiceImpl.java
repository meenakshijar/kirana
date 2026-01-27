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
import com.example.kirana.security.StoreAccessValidator;
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

/**
 * The type Transaction service.
 */
@Service
public class TransactionServiceImpl implements TransactionService {

    private final TransactionLineItemsDao transactionLineItemsDao;
    private final TransactionsRepository transactionsRepository;
    private final FxRateService fxRateService;
    private final ProductsRepository productsRepository;
    private final StoreRepository storeRepository;
    private final RedissonLockService redissonLockService;
    private final StoreAccessValidator storeAccessValidator;

    /**
     * Instantiates a new Transaction service.
     *
     * @param transactionLineItemsDao the transaction line items dao
     * @param transactionsRepository  the transactions repository
     * @param fxRateService           the fx rate service
     * @param productsRepository      the products repository
     * @param storeRepository         the store repository
     * @param redissonLockService     the redisson lock service
     * @param storeAccessValidator    the store access validator
     */
    public TransactionServiceImpl(TransactionLineItemsDao transactionLineItemsDao,
                                  TransactionsRepository transactionsRepository,
                                  FxRateService fxRateService,
                                  ProductsRepository productsRepository,
                                  StoreRepository storeRepository,
                                  RedissonLockService redissonLockService,
                                  StoreAccessValidator storeAccessValidator) {
        this.transactionLineItemsDao = transactionLineItemsDao;
        this.transactionsRepository = transactionsRepository;
        this.fxRateService = fxRateService;
        this.productsRepository = productsRepository;
        this.storeRepository = storeRepository;
        this.redissonLockService = redissonLockService;
        this.storeAccessValidator = storeAccessValidator;
    }

    @Transactional
    @Override
    public TransactionResponse createTransaction(TransactionRequest request) {

        validateTransactionRequest(request);

        String storeId = request.getStoreId();
        storeAccessValidator.validateStoreAccess(storeId);

        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new RuntimeException("Store not found: " + storeId));

        String baseCurrency = requireValue(store.getBaseCurrency(), "Base currency not configured for store: " + storeId);

        String lockKey = "lock:transaction:store:" + storeId;
        RLock lock = redissonLockService.lock(lockKey, 2, 15);

        if (lock == null) {
            throw new RuntimeException("Another transaction is already processing for this store. Try again.");
        }

        try {
            String transactionId = "TXN_" + UUID.randomUUID();
            TransactionType type = TransactionType.valueOf(request.getTransactionType().toUpperCase());
            LocalDateTime now = LocalDateTime.now();

            List<TransactionLineItems> itemsToSave = request.getItems().stream()
                    .map(item -> validateAndBuildLineItem(item, storeId, transactionId, type, baseCurrency, now))
                    .toList();

            transactionLineItemsDao.saveAll(itemsToSave);

            BigDecimal txnTotal = itemsToSave.stream()
                    .map(TransactionLineItems::getTotalProductAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            Transactions txn = new Transactions();
            txn.setTransactionId(transactionId);
            txn.setStoreId(storeId);
            txn.setTransactionType(type);
            txn.setTotalAmount(txnTotal);
            txn.setBaseCurrency(baseCurrency);
            txn.setCreatedAt(now);
            transactionsRepository.save(txn);

            return buildResponse(transactionId, storeId);

        } finally {
            redissonLockService.unlock(lock);
        }
    }

    // ---------------- HELPERS ----------------

    private void validateTransactionRequest(TransactionRequest request) {
        requireValue(request.getStoreId(), "storeId is required");
        requireValue(request.getTransactionType(), "transactionType is required");

        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new RuntimeException("Transaction items are required");
        }
    }

    private TransactionLineItems validateAndBuildLineItem(TransactionItemsRequest item,
                                                          String storeId,
                                                          String transactionId,
                                                          TransactionType type,
                                                          String baseCurrency,
                                                          LocalDateTime now) {

        requireValue(item.getProductId(), "productId is required");
        requirePositive(item.getQuantity(), "Invalid quantity for product: " + item.getProductId());
        requirePositive(item.getAmount(), "Invalid amount for product: " + item.getProductId());
        requireValue(item.getCurrency(), "Currency is required for product: " + item.getProductId());

        var product = productsRepository.findById(item.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found: " + item.getProductId()));

        if (!storeId.equals(product.getStoreId())) {
            throw new RuntimeException("Product " + item.getProductId() + " does not belong to store " + storeId);
        }

        BigDecimal originalTotal = item.getAmount().multiply(BigDecimal.valueOf(item.getQuantity()));
        BigDecimal rate = fxRateService.getFxRate(item.getCurrency(), baseCurrency);
        BigDecimal baseTotal = originalTotal.multiply(rate);

        TransactionLineItems li = new TransactionLineItems();
        li.setTransactionItemId("TXN_ITEM_" + UUID.randomUUID());
        li.setTransactionId(transactionId);

        li.setStoreId(storeId);
        li.setTransactionType(type);

        li.setProductId(item.getProductId());
        li.setQuantity(item.getQuantity());

        li.setPricePerItem(item.getAmount());
        li.setOriginalCurrency(item.getCurrency());

        li.setBaseCurrency(baseCurrency);
        li.setConversionRate(rate);

        li.setTotalProductAmount(baseTotal);
        li.setCreatedAt(now);

        return li;
    }

    private TransactionResponse buildResponse(String transactionId, String storeId) {
        TransactionResponse response = new TransactionResponse();
        response.setTransactionId(transactionId);
        response.setStoreId(storeId);
        response.setMessage("Transaction recorded successfully");
        return response;
    }

    private String requireValue(String val, String message) {
        if (val == null || val.isBlank()) throw new RuntimeException(message);
        return val;
    }

    private void requirePositive(Integer value, String message) {
        if (value == null || value <= 0) throw new RuntimeException(message);
    }

    private void requirePositive(BigDecimal value, String message) {
        if (value == null || value.compareTo(BigDecimal.ZERO) <= 0) throw new RuntimeException(message);
    }

    // ------------------------------------------------

    @Override
    public List<TransactionSummary> getTransactionsByStoreId(String storeId) {
        return transactionsRepository.findByStoreId(storeId).stream().map(view -> {
            TransactionSummary summary = new TransactionSummary();
            summary.setTransactionId(view.getTransactionId());
            summary.setTransactionType(view.getTransactionType().name());
            summary.setTotalAmount(view.getTotalAmount());
            summary.setBaseCurrency(view.getBaseCurrency());
            summary.setCreatedAt(view.getCreatedAt());
            return summary;
        }).toList();
    }

    @Override
    public TransactionDetailItemsResponse getTransactionByTransactionId(String transactionId) {

        List<TransactionLineItems> dbItems = transactionLineItemsDao.findByTransactionId(transactionId);

        if (dbItems.isEmpty()) {
            throw new RuntimeException("Transaction not found: " + transactionId);
        }

        TransactionLineItems first = dbItems.get(0);

        BigDecimal totalAmount = BigDecimal.ZERO;
        List<TransactionDetailItemsRequest> items = new ArrayList<>();

        for (TransactionLineItems li : dbItems) {
            TransactionDetailItemsRequest detail = new TransactionDetailItemsRequest();
            detail.setProductId(li.getProductId());
            detail.setQuantity(li.getQuantity());
            detail.setOriginalAmount(li.getPricePerItem());
            detail.setOriginalCurrency(li.getOriginalCurrency());
            detail.setExchangeRateUsed(li.getConversionRate());
            detail.setBaseAmount(li.getTotalProductAmount());

            totalAmount = totalAmount.add(li.getTotalProductAmount());
            items.add(detail);
        }

        TransactionDetailItemsResponse response = new TransactionDetailItemsResponse();
        response.setTransactionId(transactionId);
        response.setStoreId(first.getStoreId());
        response.setTransactionType(first.getTransactionType().name());
        response.setBaseCurrency(first.getBaseCurrency());
        response.setItems(items);
        response.setTotalAmount(totalAmount);

        return response;
    }
}
