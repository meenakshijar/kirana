package com.example.kirana.service;

import com.example.kirana.dto.ProductRequest;
import com.example.kirana.dto.ProductResponse;
import com.example.kirana.model.mongo.Products;
import com.example.kirana.repository.mongo.ProductsRepository;
import com.example.kirana.repository.mongo.StoreRepository;
import com.example.kirana.security.StoreAccessValidator;
import com.example.kirana.service.impl.ProductServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @InjectMocks
    private ProductServiceImpl productService;

    @Mock
    private ProductsRepository productRepository;

    @Mock
    private StoreRepository storeRepository;

    @Mock
    private StoreAccessValidator storeAccessValidator;

    // -----------------------------
    //SUCCESS CASE
    // -----------------------------
    @Test
    void createProduct_success() {

        ProductRequest request = validRequest();

        doNothing().when(storeAccessValidator)
                .validateStoreAccess("STORE_1");

        when(storeRepository.existsById("STORE_1"))
                .thenReturn(true);

        when(productRepository.save(any(Products.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ProductResponse response = productService.createProduct(request);

        assertNotNull(response);
        assertEquals("STORE_1", response.getStoreId());
        assertEquals("Soap", response.getProductName());

        verify(productRepository).save(any(Products.class));
    }

    // -----------------------------
    //STORE NOT FOUND
    // -----------------------------
    @Test
    void createProduct_storeNotFound_shouldFail() {

        ProductRequest request = validRequest();

        doNothing().when(storeAccessValidator)
                .validateStoreAccess("STORE_1");

        when(storeRepository.existsById("STORE_1"))
                .thenReturn(false);

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> productService.createProduct(request)
        );

        assertEquals("Store not found: STORE_1", ex.getMessage());
        verify(productRepository, never()).save(any());
    }

    // -----------------------------
    // ACCESS DENIED
    // -----------------------------
    @Test
    void createProduct_storeAccessDenied_shouldFail() {

        ProductRequest request = validRequest();

        doThrow(new RuntimeException("Access denied"))
                .when(storeAccessValidator)
                .validateStoreAccess("STORE_1");

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> productService.createProduct(request)
        );

        assertEquals("Access denied", ex.getMessage());
        verify(productRepository, never()).save(any());
    }

    // -----------------------------
    //  CREATED AT SET
    // -----------------------------
    @Test
    void createProduct_createdAtIsSet() {

        ProductRequest request = validRequest();

        doNothing().when(storeAccessValidator)
                .validateStoreAccess("STORE_1");

        when(storeRepository.existsById("STORE_1"))
                .thenReturn(true);

        when(productRepository.save(any(Products.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        productService.createProduct(request);

        ArgumentCaptor<Products> captor =
                ArgumentCaptor.forClass(Products.class);

        verify(productRepository).save(captor.capture());

        Products saved = captor.getValue();
        assertNotNull(saved.getProductCreatedAt());
    }

    // -----------------------------
    // GET PRODUCTS SUCCESS
    // -----------------------------
    @Test
    void getProductsByStoreId_success() {

        doNothing().when(storeAccessValidator)
                .validateStoreAccess("STORE_1");

        when(productRepository.findByStoreId("STORE_1"))
                .thenReturn(List.of(new Products(), new Products()));

        List<Products> result =
                productService.getProductsByStoreId("STORE_1");

        assertEquals(2, result.size());
    }

    // -----------------------------
    // GET PRODUCTS ACCESS DENIED
    // -----------------------------
    @Test
    void getProductsByStoreId_accessDenied_shouldFail() {

        doThrow(new RuntimeException("Access denied"))
                .when(storeAccessValidator)
                .validateStoreAccess("STORE_1");

        assertThrows(
                RuntimeException.class,
                () -> productService.getProductsByStoreId("STORE_1")
        );

        verify(productRepository, never())
                .findByStoreId(any());
    }

    // -----------------------------
    // TEST DATA BUILDER
    // -----------------------------
    private ProductRequest validRequest() {
        ProductRequest req = new ProductRequest();
        req.setStoreId("STORE_1");
        req.setProductName("Soap");
        req.setCostPrice(BigDecimal.valueOf(20));
        req.setSellingPrice(BigDecimal.valueOf(30));
        req.setMrp(BigDecimal.valueOf(35));
        req.setDiscountPercentage(BigDecimal.valueOf(5));
        req.setUnit("PCS");
        req.setBrand("Dove");
        req.setCategory("Personal Care");
        return req;
    }
}
