package com.example.kirana.service.impl;

import com.example.kirana.dto.ProductRequest;
import com.example.kirana.dto.ProductResponse;
import com.example.kirana.model.mongo.Products;
import com.example.kirana.repository.mongo.ProductsRepository;
import com.example.kirana.repository.mongo.StoreRepository;
import com.example.kirana.security.StoreAccessValidator;
import com.example.kirana.service.ProductService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class ProductServiceImpl implements ProductService {

    private final ProductsRepository productRepository;
    private final StoreRepository storeRepository;
    private final StoreAccessValidator storeAccessValidator;


    public ProductServiceImpl(ProductsRepository productRepository, StoreRepository storeRepository, StoreAccessValidator storeAccessValidator) {
        this.productRepository = productRepository;
        this.storeRepository = storeRepository;
        this.storeAccessValidator = storeAccessValidator;
    }

    @Override
    public ProductResponse createProduct(ProductRequest request) {
        storeAccessValidator.validateStoreAccess(request.getStoreId());
        boolean storeExists = storeRepository.existsById(request.getStoreId());
        if (!storeExists) {
            throw new RuntimeException("Store not found: " + request.getStoreId());
        }

        String productId = "PROD_" + UUID.randomUUID();
        Products product = new Products();
        product.setProductId(productId);
        product.setStoreId(request.getStoreId());

        product.setProductName(request.getProductName());

        product.setCostPrice(request.getCostPrice());
        product.setSellingPrice(request.getSellingPrice());
        product.setMrp(request.getMrp());
        product.setDiscountPercentage(request.getDiscountPercentage());

        product.setUnit(request.getUnit());
        product.setBrand(request.getBrand());
        product.setCategory(request.getCategory());

        product.setProductCreatedAt(LocalDateTime.now());

        productRepository.save(product);

        ProductResponse response = new ProductResponse();
        response.setProductId(product.getProductId());
        response.setStoreId(product.getStoreId());
        response.setProductName(product.getProductName());
        response.setCategory(product.getCategory());
        response.setMessage("Product created successfully");

        return response;
    }

    @Override
    public List<Products> getProductsByStoreId(String storeId) {
        storeAccessValidator.validateStoreAccess(storeId);
        return productRepository.findByStoreId(storeId);
    }
}
