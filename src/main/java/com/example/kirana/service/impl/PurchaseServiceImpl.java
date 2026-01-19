package com.example.kirana.service.impl;

import com.example.kirana.dao.PurchaseLineItemsDao;
import com.example.kirana.dto.PurchaseItemsRequest;
import com.example.kirana.dto.PurchaseRequest;
import com.example.kirana.dto.PurchaseResponse;
import com.example.kirana.model.TransactionType;
import com.example.kirana.model.postgres.PurchaseLineItems;
import com.example.kirana.service.FxRateService;
import com.example.kirana.service.PurchaseService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class PurchaseServiceImpl implements PurchaseService {

    private final PurchaseLineItemsDao purchaseLineItemsDao;
    private final FxRateService fxRateService;

    public PurchaseServiceImpl(PurchaseLineItemsDao purchaseLineItemsDao,
                               FxRateService fxRateService) {
        this.purchaseLineItemsDao = purchaseLineItemsDao;
        this.fxRateService = fxRateService;
    }

    @Override
    public PurchaseResponse createPurchase(PurchaseRequest request) {

        String purchaseId = "PUR_" + UUID.randomUUID();

        TransactionType type = TransactionType.valueOf(request.getTransactionType()); // mostly DEBIT

        List<PurchaseLineItems> itemsToSave = new ArrayList<>();

        // ✅ For now hardcode baseCurrency (later fetch from Store in Mongo)
        String baseCurrency = "INR";

        for (PurchaseItemsRequest item : request.getItems()) {

            BigDecimal originalTotalAmount = item.getAmount()
                    .multiply(BigDecimal.valueOf(item.getQuantity()));

            // ✅ get rate from Redis cache (or API fallback)
            BigDecimal rate = fxRateService.getFxRate(item.getCurrency(), baseCurrency);

            // ✅ convert to base currency amount
            BigDecimal baseTotalAmount = originalTotalAmount.multiply(rate);

            PurchaseLineItems purchaseLineItems = new PurchaseLineItems();
            purchaseLineItems.setPurchaseItemId("PUR_ITEM_" + UUID.randomUUID());
            purchaseLineItems.setPurchaseId(purchaseId);

            purchaseLineItems.setStoreId(request.getStoreId());
            purchaseLineItems.setTransactionType(type);

            purchaseLineItems.setProductId(item.getProductId());
            purchaseLineItems.setQuantity(item.getQuantity());

            purchaseLineItems.setPricePerItem(item.getAmount());
            purchaseLineItems.setOriginalCurrency(item.getCurrency());

            // ✅ Set baseCurrency + conversionRate
            purchaseLineItems.setBaseCurrency(baseCurrency);
            purchaseLineItems.setConversionRate(rate);

            // ✅ store total in base currency
            purchaseLineItems.setTotalAmount(baseTotalAmount);

            purchaseLineItems.setCreatedAt(LocalDateTime.now());

            itemsToSave.add(purchaseLineItems);
        }

        purchaseLineItemsDao.saveAll(itemsToSave);

        PurchaseResponse response = new PurchaseResponse();
        response.setPurchaseId(purchaseId);
        response.setStoreId(request.getStoreId());
        response.setMessage("Purchase recorded successfully");

        return response;
    }
}
