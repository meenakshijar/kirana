package com.example.kirana.service.impl;

import com.example.kirana.dao.PurchaseLineItemsDao;
import com.example.kirana.dto.*;
import com.example.kirana.model.TransactionType;
import com.example.kirana.model.postgres.PurchaseLineItems;
import com.example.kirana.repository.mongo.StoreRepository;
import com.example.kirana.service.FxRateService;
import com.example.kirana.service.PurchaseService;
import org.redisson.api.RLock;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import com.example.kirana.lock.RedissonLockService;

@Service
public class PurchaseServiceImpl implements PurchaseService {

    private final PurchaseLineItemsDao purchaseLineItemsDao;
    private final FxRateService fxRateService;
    private final StoreRepository storeRepository;
    private final RedissonLockService redissonLockService;


    public PurchaseServiceImpl(PurchaseLineItemsDao purchaseLineItemsDao,
                               FxRateService fxRateService, StoreRepository storeRepository, RedissonLockService redissonLockService) {
        this.purchaseLineItemsDao = purchaseLineItemsDao;
        this.fxRateService = fxRateService;
        this.storeRepository = storeRepository;
        this.redissonLockService = redissonLockService;
    }
    @Transactional
    @Override
    public PurchaseResponse createPurchase(PurchaseRequest request) {
        if (!storeRepository.existsById(request.getStoreId())) {
            throw new RuntimeException("Store not found: " + request.getStoreId());
        }
        String purchaseId = "PUR_" + UUID.randomUUID();

        TransactionType type = TransactionType.valueOf(request.getTransactionType()); // mostly DEBIT

        List<PurchaseLineItems> itemsToSave = new ArrayList<>();


        String baseCurrency = storeRepository.findById(request.getStoreId())
                .orElseThrow(() -> new RuntimeException("Store not found: " + request.getStoreId()))
                .getBaseCurrency();

        String lockKey = "lock:purchase:store:" + request.getStoreId();
        RLock lock = redissonLockService.lock(lockKey, 2, 15);

        if (lock == null) {
            throw new RuntimeException("Another purchase is already processing for this store. Try again.");
        }
        try{
        for (PurchaseItemsRequest item : request.getItems()) {

            BigDecimal originalTotalAmount = item.getAmount()
                    .multiply(BigDecimal.valueOf(item.getQuantity()));


            BigDecimal rate = fxRateService.getFxRate(item.getCurrency(), baseCurrency);


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


            purchaseLineItems.setBaseCurrency(baseCurrency);
            purchaseLineItems.setConversionRate(rate);


            purchaseLineItems.setTotalAmount(baseTotalAmount);

            purchaseLineItems.setCreatedAt(LocalDateTime.now());

            itemsToSave.add(purchaseLineItems);
        }


        purchaseLineItemsDao.saveAll(itemsToSave);

        PurchaseResponse response = new PurchaseResponse();
        response.setPurchaseId(purchaseId);
        response.setStoreId(request.getStoreId());
        response.setMessage("Purchase recorded successfully");

        return response;} finally {
            redissonLockService.unlock(lock);
        }
    }

    @Override
    public PurchaseDetailResponse getPurchaseByPurchaseId(String purchaseId) {

        List<PurchaseLineItems> dbItems = purchaseLineItemsDao.findByPurchaseId(purchaseId);

        if (dbItems.isEmpty()) {
            throw new RuntimeException("Purchase not found: " + purchaseId);
        }

        String storeId = dbItems.get(0).getStoreId();
        String transactionType = dbItems.get(0).getTransactionType().name();
        String baseCurrency = dbItems.get(0).getBaseCurrency();

        BigDecimal totalAmount = BigDecimal.ZERO;

        List<PurchaseDetailItem> items = new ArrayList<>();

        for (PurchaseLineItems li : dbItems) {
            PurchaseDetailItem item = new PurchaseDetailItem();
            item.setProductId(li.getProductId());
            item.setQuantity(li.getQuantity());
            item.setOriginalAmount(li.getPricePerItem());
            item.setOriginalCurrency(li.getOriginalCurrency());
            item.setExchangeRateUsed(li.getConversionRate());
            item.setBaseAmount(li.getTotalAmount());

            totalAmount = totalAmount.add(li.getTotalAmount());
            items.add(item);
        }

        PurchaseDetailResponse response = new PurchaseDetailResponse();
        response.setPurchaseId(purchaseId);
        response.setStoreId(storeId);
        response.setTransactionType(transactionType);
        response.setBaseCurrency(baseCurrency);
        response.setItems(items);
        response.setTotalAmount(totalAmount);

        return response;
    }

}
