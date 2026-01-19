package com.example.kirana.service.impl;

import com.example.kirana.dao.TransactionLineItemsDao;
import com.example.kirana.dto.TransactionDetailItemsRequest;
import com.example.kirana.dto.TransactionDetailItemsResponse;
import com.example.kirana.dto.TransactionItemsRequest;
import com.example.kirana.dto.TransactionRequest;
import com.example.kirana.dto.TransactionResponse;
import com.example.kirana.dto.TransactionSummary;
import com.example.kirana.model.TransactionType;
import com.example.kirana.model.mongo.Transactions;
import com.example.kirana.model.postgres.TransactionLineItems;
import com.example.kirana.repository.mongo.TransactionsRepository;
import com.example.kirana.service.FxRateService;
import com.example.kirana.service.TransactionService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class TransactionServiceImpl implements TransactionService {

    private final TransactionLineItemsDao transactionLineItemsDao;
    private final TransactionsRepository transactionsRepository; // Mongo view (read-only)
    private final FxRateService fxRateService;

    public TransactionServiceImpl(TransactionLineItemsDao transactionLineItemsDao,
                                  TransactionsRepository transactionsRepository,
                                  FxRateService fxRateService) {
        this.transactionLineItemsDao = transactionLineItemsDao;
        this.transactionsRepository = transactionsRepository;
        this.fxRateService = fxRateService;
    }

    @Override
    public TransactionResponse createTransaction(TransactionRequest request) {

        String transactionId = "TXN_" + UUID.randomUUID();
        TransactionType type = TransactionType.valueOf(request.getTransactionType());

        List<TransactionLineItems> itemsToSave = new ArrayList<>();

        // ✅ For now hardcode store base currency (later fetch from store in MongoDB)
        String baseCurrency = "INR";

        for (TransactionItemsRequest item : request.getItems()) {

            // ✅ original total = amount * quantity (in original currency)
            BigDecimal originalTotal = item.getAmount()
                    .multiply(BigDecimal.valueOf(item.getQuantity()));

            // ✅ conversion rate from Redis cache (or fallback API)
            BigDecimal rate = fxRateService.getFxRate(item.getCurrency(), baseCurrency);

            // ✅ convert to base currency
            BigDecimal baseTotal = originalTotal.multiply(rate);

            TransactionLineItems lineItems = new TransactionLineItems();
            lineItems.setTransactionItemId("TXN_ITEM_" + UUID.randomUUID());
            lineItems.setTransactionId(transactionId);

            lineItems.setStoreId(request.getStoreId());
            lineItems.setTransactionType(type);

            lineItems.setProductId(item.getProductId());
            lineItems.setQuantity(item.getQuantity());

            lineItems.setPricePerItem(item.getAmount());          // original price/amount
            lineItems.setOriginalCurrency(item.getCurrency());    // original currency

            // ✅ base conversion
            lineItems.setBaseCurrency(baseCurrency);
            lineItems.setConversionRate(rate);

            // ✅ store total in base currency
            lineItems.setTotalProductAmount(baseTotal);

            lineItems.setCreatedAt(LocalDateTime.now());

            itemsToSave.add(lineItems);
        }

        transactionLineItemsDao.saveAll(itemsToSave);

        TransactionResponse response = new TransactionResponse();
        response.setTransactionId(transactionId);
        response.setStoreId(request.getStoreId());
        response.setMessage("Transaction recorded successfully");

        return response;
    }

    @Override
    public List<TransactionSummary> getTransactionsByStoreId(String storeId) {

        // This reads from MongoDB VIEW: "transactions"
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
