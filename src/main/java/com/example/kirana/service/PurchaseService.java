package com.example.kirana.service;

import com.example.kirana.dto.PurchaseRequest;
import com.example.kirana.dto.PurchaseResponse;

public interface PurchaseService {
    PurchaseResponse createPurchase(PurchaseRequest request);
}
