package com.example.kirana.controller;

import com.example.kirana.dto.ProductRequest;
import com.example.kirana.dto.ProductResponse;
import com.example.kirana.model.mongo.Products;
import com.example.kirana.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    //  ADMIN / SUPER_ADMIN
    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(@Valid @RequestBody ProductRequest request) {
        return ResponseEntity.ok(productService.createProduct(request));
    }

    //  USER / ADMIN / SUPER_ADMIN
    @GetMapping("/{storeId}")
    public ResponseEntity<List<Products>> getProductsByStoreId(@PathVariable String storeId) {
        return ResponseEntity.ok(productService.getProductsByStoreId(storeId));
    }
}
