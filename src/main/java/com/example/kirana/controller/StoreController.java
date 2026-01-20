package com.example.kirana.controller;

import com.example.kirana.dto.StoreRequest;
import com.example.kirana.dto.StoreResponse;
import com.example.kirana.model.mongo.Store;
import com.example.kirana.service.StoreService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/store")
public class StoreController {

    private final StoreService storeService;

    public StoreController(StoreService storeService) {
        this.storeService = storeService;
    }

    // ✅ SUPER_ADMIN
    @PostMapping
    public ResponseEntity<StoreResponse> createStore(@RequestBody StoreRequest request) {
        return ResponseEntity.ok(storeService.createStore(request));
    }

    // ✅ SUPER_ADMIN
    @GetMapping("/{storeId}")
    public ResponseEntity<Store> getStore(@PathVariable String storeId) {
        return ResponseEntity.ok(storeService.getStoreById(storeId));
    }
}
