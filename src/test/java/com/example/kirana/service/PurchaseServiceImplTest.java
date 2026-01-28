package com.example.kirana.service;

import com.example.kirana.dao.PurchaseLineItemsDao;
import com.example.kirana.dto.PurchaseItemsRequest;
import com.example.kirana.dto.PurchaseRequest;
import com.example.kirana.lock.RedissonLockService;
import com.example.kirana.model.mongo.Store;
import com.example.kirana.repository.mongo.StoreRepository;
import com.example.kirana.security.StoreAccessValidator;
import com.example.kirana.service.impl.PurchaseServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.redisson.api.RLock;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PurchaseServiceImplTest {

    @InjectMocks
    private PurchaseServiceImpl purchaseService;

    @Mock
    private PurchaseLineItemsDao purchaseLineItemsDao;

    @Mock
    private FxRateService fxRateService;

    @Mock
    private StoreRepository storeRepository;

    @Mock
    private RedissonLockService redissonLockService;

    @Mock
    private StoreAccessValidator storeAccessValidator;

    @Mock
    private RLock rLock;

    @Test
    void createPurchase_success() {

        // -------- GIVEN --------
        PurchaseRequest request = validRequest();

        // store exists check
        when(storeRepository.existsById("STORE_1"))
                .thenReturn(true);

        // store access validation
        doNothing().when(storeAccessValidator)
                .validateStoreAccess("STORE_1");

        // base currency fetch
        Store store = new Store();
        store.setStoreId("STORE_1");
        store.setBaseCurrency("INR");

        when(storeRepository.findById("STORE_1"))
                .thenReturn(Optional.of(store));

        // distributed lock
        when(redissonLockService.lock(anyString(), anyLong(), anyLong()))
                .thenReturn(rLock);

        doNothing().when(redissonLockService).unlock(rLock);

        // FX rate
        when(fxRateService.getFxRate(anyString(), eq("INR")))
                .thenReturn(BigDecimal.ONE);

        //️save items
        when(purchaseLineItemsDao.saveAll(anyList()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // -------- WHEN / THEN --------
        assertDoesNotThrow(() ->
                purchaseService.createPurchase(request)
        );

        verify(storeRepository).existsById("STORE_1");
        verify(storeRepository).findById("STORE_1");
        verify(purchaseLineItemsDao).saveAll(anyList());
        verify(redissonLockService).unlock(rLock);
    }
    @Test
    void createPurchase_storeNotFound_shouldFail() {

        PurchaseRequest request = validRequest();

        when(storeRepository.existsById("STORE_1"))
                .thenReturn(false);

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> purchaseService.createPurchase(request)
        );

        assertEquals("Store not found: STORE_1", ex.getMessage());

        verify(storeAccessValidator, never()).validateStoreAccess(any());
        verify(purchaseLineItemsDao, never()).saveAll(any());
    }
    @Test
    void createPurchase_storeAccessDenied_shouldFail() {

        PurchaseRequest request = validRequest();

        when(storeRepository.existsById("STORE_1"))
                .thenReturn(true);

        doThrow(new RuntimeException("Access denied"))
                .when(storeAccessValidator)
                .validateStoreAccess("STORE_1");

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> purchaseService.createPurchase(request)
        );

        assertEquals("Access denied", ex.getMessage());

        verify(purchaseLineItemsDao, never()).saveAll(any());
    }
    @Test
    void createPurchase_invalidTransactionType_shouldFail() {

        PurchaseRequest request = validRequest();
        request.setTransactionType("INVALID_TYPE");

        when(storeRepository.existsById("STORE_1"))
                .thenReturn(true);

        doNothing().when(storeAccessValidator)
                .validateStoreAccess("STORE_1");

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> purchaseService.createPurchase(request)
        );

        assertTrue(ex instanceof IllegalArgumentException);
    }
    @Test
    void createPurchase_baseCurrencyMissing_shouldFail() {

        PurchaseRequest request = validRequest();

        when(storeRepository.existsById("STORE_1"))
                .thenReturn(true);

        doNothing().when(storeAccessValidator)
                .validateStoreAccess("STORE_1");

        Store store = new Store();
        store.setStoreId("STORE_1");
        store.setBaseCurrency(null);

        when(storeRepository.findById("STORE_1"))
                .thenReturn(Optional.of(store));

        when(redissonLockService.lock(anyString(), anyLong(), anyLong()))
                .thenReturn(mock(RLock.class));

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> purchaseService.createPurchase(request)
        );

        assertNotNull(ex);
    }
    @Test
    void createPurchase_lockUnavailable_shouldFail() {

        PurchaseRequest request = validRequest();

        when(storeRepository.existsById("STORE_1"))
                .thenReturn(true);

        doNothing().when(storeAccessValidator)
                .validateStoreAccess("STORE_1");

        Store store = new Store();
        store.setStoreId("STORE_1");
        store.setBaseCurrency("INR");

        when(storeRepository.findById("STORE_1"))
                .thenReturn(Optional.of(store));

        when(redissonLockService.lock(anyString(), anyLong(), anyLong()))
                .thenReturn(null);

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> purchaseService.createPurchase(request)
        );

        assertEquals(
                "Another purchase is already processing for this store. Try again.",
                ex.getMessage()
        );

        verify(purchaseLineItemsDao, never()).saveAll(any());
    }

    @Test
    void createPurchase_fxRateFailure_shouldFail() {

        PurchaseRequest request = validRequest();

        when(storeRepository.existsById("STORE_1"))
                .thenReturn(true);

        doNothing().when(storeAccessValidator)
                .validateStoreAccess("STORE_1");

        Store store = new Store();
        store.setStoreId("STORE_1");
        store.setBaseCurrency("INR");

        when(storeRepository.findById("STORE_1"))
                .thenReturn(Optional.of(store));

        when(redissonLockService.lock(anyString(), anyLong(), anyLong()))
                .thenReturn(mock(RLock.class));

        when(fxRateService.getFxRate(anyString(), eq("INR")))
                .thenThrow(new RuntimeException("FX service down"));

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> purchaseService.createPurchase(request)
        );

        assertEquals("FX service down", ex.getMessage());
    }

    private PurchaseRequest validRequest() {
        PurchaseItemsRequest item = new PurchaseItemsRequest();
        item.setProductId("PROD_1");
        item.setQuantity(2);
        item.setAmount(BigDecimal.valueOf(100));
        item.setCurrency("INR");

        PurchaseRequest request = new PurchaseRequest();
        request.setStoreId("STORE_1");
        request.setTransactionType("DEBIT"); // MUST match enum
        request.setItems(List.of(item));

        return request;
    }
}
