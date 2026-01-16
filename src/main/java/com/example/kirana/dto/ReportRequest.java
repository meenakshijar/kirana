package com.example.kirana.dto;


import lombok.Data;

@Data
public class ReportRequest {

    private String storeId;
    private String period; // WEEKLY / MONTHLY / YEARLY
    private PeriodTime periodTime;

    // getters + setters
}
