package com.example.kirana.service;


import com.example.kirana.dao.TransactionLineItemsDao;
import com.example.kirana.dto.TransactionItemsRequest;
import com.example.kirana.dto.TransactionRequest;
import com.example.kirana.dto.TransactionResponse;
import com.example.kirana.dto.TransactionSummary;
import com.example.kirana.lock.RedissonLockService;
import com.example.kirana.model.TransactionType;
import com.example.kirana.model.mongo.Products;
import com.example.kirana.model.mongo.Store;

import com.example.kirana.model.mongo.Transactions;
import com.example.kirana.repository.mongo.ProductsRepository;
import com.example.kirana.repository.mongo.StoreRepository;
import com.example.kirana.repository.mongo.TransactionsRepository;
import com.example.kirana.security.StoreAccessValidator;
import com.example.kirana.service.impl.TransactionServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import org.mockito.junit.jupiter.MockitoExtension;
import org.redisson.api.RLock;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static net.logstash.logback.stacktrace.StackElementFilter.any;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;





@ExtendWith(MockitoExtension.class)
class TransactionServiceImplTest {

    @InjectMocks
    private TransactionServiceImpl transactionService;

    @Mock
    private TransactionLineItemsDao transactionLineItemsDao;

    @Mock
    private TransactionsRepository transactionsRepository;

    @Mock
    private FxRateService fxRateService;

    @Mock
    private ProductsRepository productsRepository;

    @Mock
    private StoreRepository storeRepository;

    @Mock
    private RedissonLockService redissonLockService;

    @Mock
    private StoreAccessValidator storeAccessValidator;

    @Mock
    private RLock rLock;
    private TransactionRequest validRequest() {
        TransactionItemsRequest item = new TransactionItemsRequest();
        item.setProductId("PROD_1");
        item.setQuantity(2);
        item.setAmount(new BigDecimal("100"));
        item.setCurrency("USD");

        TransactionRequest request = new TransactionRequest();
        request.setStoreId("STORE_1");
        request.setTransactionType("CREDIT");
        request.setItems(List.of(item));

        return request;
    }

    @Test
    void createTransaction_success() {

        TransactionRequest request = validRequest();

        Store store = new Store();
        store.setStoreId("STORE_1");
        store.setBaseCurrency("INR");

        when(storeRepository.findById("STORE_1"))
                .thenReturn(Optional.of(store));

        // ONLY lock stub — nothing else
        when(redissonLockService.lock(anyString(), anyLong(), anyLong()))
                .thenReturn(rLock);

        Products product = new Products();
        product.setProductId("PROD_1");
        product.setStoreId("STORE_1");

        when(productsRepository.findById("PROD_1"))
                .thenReturn(Optional.of(product));

        when(fxRateService.getFxRate("USD", "INR"))
                .thenReturn(new BigDecimal("80"));

        TransactionResponse response =
                transactionService.createTransaction(request);

        assertNotNull(response.getTransactionId());
        assertEquals("STORE_1", response.getStoreId());

        verify(transactionLineItemsDao).saveAll(anyList());
        verify(redissonLockService).unlock(rLock);
    }


    @Test
    void createTransaction_lockUnavailable_shouldFail() {

        TransactionRequest request = validRequest();

        Store store = new Store();
        store.setStoreId("STORE_1");
        store.setBaseCurrency("INR");

        when(storeRepository.findById("STORE_1"))
                .thenReturn(Optional.of(store));

        when(redissonLockService.lock(anyString(), anyLong(), anyLong()))
                .thenReturn(null); // lock unavailable

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> transactionService.createTransaction(request)
        );

        assertEquals(
                "Another transaction is already processing for this store. Try again.",
                ex.getMessage()
        );

        verify(redissonLockService, never()).unlock(rLock);


    }

    @Test
    void createTransaction_productFromDifferentStore() {

        TransactionRequest request = validRequest();

        Store store = new Store();
        store.setStoreId("STORE_1");
        store.setBaseCurrency("INR");

        when(storeRepository.findById("STORE_1"))
                .thenReturn(Optional.of(store));

        when(redissonLockService.lock(anyString(), anyLong(), anyLong()))
                .thenReturn(rLock);

        Products product = new Products();
        product.setProductId("PROD_1");
        product.setStoreId("STORE_2"); // mismatch

        when(productsRepository.findById("PROD_1"))
                .thenReturn(Optional.of(product));

        assertThrows(
                RuntimeException.class,
                () -> transactionService.createTransaction(request)
        );

        verify(redissonLockService).unlock(rLock);
    }

    @Test
    void createTransaction_baseCurrencyMissing() {

        TransactionRequest request = validRequest();

        Store store = new Store();
        store.setStoreId("STORE_1"); // baseCurrency NOT set

        when(storeRepository.findById("STORE_1"))
                .thenReturn(Optional.of(store));

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> transactionService.createTransaction(request)
        );

        assertTrue(ex.getMessage().contains("Base currency not configured"));

        verify(redissonLockService, never()).lock(anyString(), anyLong(), anyLong());
    }

    @Test
    void getTransactionsByStoreId_success() {

        Transactions txn = new Transactions();
        txn.setTransactionId("TXN_1");
        txn.setStoreId("STORE_1");
        txn.setTransactionType(TransactionType.CREDIT);
        txn.setTotalAmount(new BigDecimal("1000"));
        txn.setBaseCurrency("INR");
        txn.setCreatedAt(LocalDateTime.now());

        when(transactionsRepository.findByStoreId("STORE_1"))
                .thenReturn(List.of(txn));

        List<TransactionSummary> result =
                transactionService.getTransactionsByStoreId("STORE_1");

        assertEquals(1, result.size());
        assertEquals("CREDIT", result.get(0).getTransactionType());
    }


    @Test
    void getTransactionByTransactionId_notFound() {

        when(transactionLineItemsDao.findByTransactionId("TXN_1"))
                .thenReturn(List.of());

        assertThrows(
                RuntimeException.class,
                () -> transactionService.getTransactionByTransactionId("TXN_1")
        );
    }

}
