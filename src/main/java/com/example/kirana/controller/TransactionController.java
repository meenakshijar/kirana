package com.example.kirana.controller;

import com.example.kirana.dto.TransactionDetailItemsResponse;

import com.example.kirana.dto.TransactionRequest;
import com.example.kirana.dto.TransactionResponse;
import com.example.kirana.dto.TransactionSummary;
import com.example.kirana.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@PreAuthorize("hasAnyRole('USER','ADMIN','SUPER_ADMIN')")

@RestController
@RequestMapping("/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    // 1) Create Transaction
    // POST /transactions
    @PostMapping
    public ResponseEntity<TransactionResponse> createTransaction(
            @Valid @RequestBody TransactionRequest request
    ) {
        TransactionResponse response = transactionService.createTransaction(request);
        return ResponseEntity.ok(response);
    }

    // 2) Get Transactions by StoreId
    // GET /transactions?storeId=STORE_001
    @GetMapping
    public ResponseEntity<List<TransactionSummary>> getTransactionsByStoreId(
            @RequestParam String storeId
    ) {
        List<TransactionSummary> response = transactionService.getTransactionsByStoreId(storeId);
        return ResponseEntity.ok(response);
    }
    //  3) Get Transaction Details by transactionId
    // GET /transactions/{transactionId}
    @GetMapping("/{transactionId}")
    public ResponseEntity<TransactionDetailItemsResponse> getTransactionByTransactionId(
            @PathVariable String transactionId
    ) {
        TransactionDetailItemsResponse response =
                transactionService.getTransactionByTransactionId(transactionId);

        return ResponseEntity.ok(response);
    }
}
