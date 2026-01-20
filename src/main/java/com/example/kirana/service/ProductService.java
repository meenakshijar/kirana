package com.example.kirana.service;

import com.example.kirana.dto.ProductRequest;
import com.example.kirana.dto.ProductResponse;
import com.example.kirana.model.mongo.Products;

import java.util.List;

public interface ProductService {

    ProductResponse createProduct(ProductRequest request);

    List<Products> getProductsByStoreId(String storeId);
}
