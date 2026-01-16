package com.example.kirana.service.impl;

import com.example.kirana.dao.PurchaseLineItemsDao;
import com.example.kirana.dto.PurchaseItemsRequest;
import com.example.kirana.dto.PurchaseRequest;
import com.example.kirana.dto.PurchaseResponse;
import com.example.kirana.model.TransactionType;
import com.example.kirana.model.postgres.PurchaseLineItems;
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

    public PurchaseServiceImpl(PurchaseLineItemsDao purchaseLineItemsDao) {
        this.purchaseLineItemsDao = purchaseLineItemsDao;
    }

    @Override
    public PurchaseResponse createPurchase(PurchaseRequest request) {

        String purchaseId = "PUR_" + UUID.randomUUID();

        TransactionType type = TransactionType.valueOf(request.getTransactionType()); // mostly DEBIT

        List<PurchaseLineItems> itemsToSave = new ArrayList<>();

        for (PurchaseItemsRequest item : request.getItems()) {

            BigDecimal totalAmount = item.getAmount()
                    .multiply(BigDecimal.valueOf(item.getQuantity()));

            PurchaseLineItems purchaseLineItems = new PurchaseLineItems();
            purchaseLineItems.setPurchaseItemId("PUR_ITEM_" + UUID.randomUUID());
            purchaseLineItems.setPurchaseId(purchaseId);

            purchaseLineItems.setStoreId(request.getStoreId());
            purchaseLineItems.setTransactionType(type);

            purchaseLineItems.setProductId(item.getProductId());
            purchaseLineItems.setQuantity(item.getQuantity());

            purchaseLineItems.setPricePerItem(item.getAmount());
            purchaseLineItems.setOriginalCurrency(item.getCurrency());

            // For now baseCurrency = originalCurrency and conversionRate = 1
            // Later you can integrate FX service like Transaction flow
            purchaseLineItems.setBaseCurrency(item.getCurrency());
            purchaseLineItems.setConversionRate(BigDecimal.ONE);

            purchaseLineItems.setTotalAmount(totalAmount);

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
