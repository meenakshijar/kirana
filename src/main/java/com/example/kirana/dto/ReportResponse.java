package com.example.kirana.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
@Data
public class ReportResponse {

    private String storeId;
    private String period;
    private String periodLabel;

    private String baseCurrency;

    private BigDecimal totalCredit;
    private BigDecimal totalDebit;
    private BigDecimal netFlow;

    private LocalDateTime generatedAt;


}
