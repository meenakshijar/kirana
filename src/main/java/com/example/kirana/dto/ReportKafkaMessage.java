package com.example.kirana.dto;

import lombok.Data;

@Data
public class ReportKafkaMessage {

    private String reportId;
    private String storeId;
    private String period;
    private PeriodTime periodTime;
}
