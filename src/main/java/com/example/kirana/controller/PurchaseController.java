package com.example.kirana.controller;

import com.example.kirana.dto.PurchaseRequest;
import com.example.kirana.dto.PurchaseResponse;
import com.example.kirana.service.PurchaseService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
@PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")

@RestController
@RequestMapping("/purchases")
public class PurchaseController {

    private final PurchaseService purchaseService;

    public PurchaseController(PurchaseService purchaseService) {
        this.purchaseService = purchaseService;
    }

    // ✅ POST /purchases
    @PostMapping
    public ResponseEntity<PurchaseResponse> createPurchase(@RequestBody PurchaseRequest request) {
        PurchaseResponse response = purchaseService.createPurchase(request);
        return ResponseEntity.ok(response);
    }
}
