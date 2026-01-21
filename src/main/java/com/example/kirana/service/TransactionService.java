package com.example.kirana.service;

import com.example.kirana.dto.TransactionDetailItemsResponse;
import com.example.kirana.dto.TransactionRequest;
import com.example.kirana.dto.TransactionResponse;
import com.example.kirana.dto.TransactionSummary;

import java.util.List;

public interface
TransactionService {

    TransactionResponse createTransaction(TransactionRequest request);

    List<TransactionSummary> getTransactionsByStoreId(String storeId);

    TransactionDetailItemsResponse getTransactionByTransactionId(String transactionId);
}
