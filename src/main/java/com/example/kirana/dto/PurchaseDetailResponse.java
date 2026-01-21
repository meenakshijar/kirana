package com.example.kirana.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class PurchaseDetailResponse {
    private String purchaseId;
    private String storeId;
    private String transactionType;
    private String baseCurrency;
    private List<PurchaseDetailItem> items;
    private BigDecimal totalAmount;
}
