package com.example.kirana.dto;


import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import com.example.kirana.dto.PeriodTime;

@Data

public class ReportRequest {

    private String storeId;
    private String period; // WEEKLY / MONTHLY / YEARLY
    private PeriodTime periodTime;


}
