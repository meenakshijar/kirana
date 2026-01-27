package com.example.kirana.controller;

import com.example.kirana.dto.PurchaseDetailResponse;
import com.example.kirana.dto.PurchaseRequest;
import com.example.kirana.dto.PurchaseResponse;
import com.example.kirana.dto.TransactionDetailItemsResponse;
import com.example.kirana.service.PurchaseService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * The type Purchase controller.
 */
@PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")

@RestController
@RequestMapping("/purchases")
public class PurchaseController {

    private final PurchaseService purchaseService;

    /**
     * Instantiates a new Purchase controller.
     *
     * @param purchaseService the purchase service
     */
    public PurchaseController(PurchaseService purchaseService) {
        this.purchaseService = purchaseService;
    }


    /**
     * Create purchase response entity.
     *
     * @param request the request
     * @return the response entity
     */
    @PostMapping
    public ResponseEntity<PurchaseResponse> createPurchase(@Valid @RequestBody PurchaseRequest request) {
        PurchaseResponse response = purchaseService.createPurchase(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Gets purchase.
     *
     * @param purchaseId the purchase id
     * @return the purchase
     */
    @GetMapping("/{purchaseId}")
    public ResponseEntity<PurchaseDetailResponse> getPurchase(@PathVariable String purchaseId) {
        return ResponseEntity.ok(purchaseService.getPurchaseByPurchaseId(purchaseId));
    }

}
