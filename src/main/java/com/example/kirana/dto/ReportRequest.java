package com.example.kirana.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import com.example.kirana.dto.PeriodTime;

@Data

public class ReportRequest {

    @NotBlank(message = "storeId is required")

    private String storeId;

    @NotBlank(message = "period is required")

    private String period;

    @NotNull(message = "periodTime is required")
    private PeriodTime periodTime;


}
