package com.example.kirana.service;

import com.example.kirana.dto.ReportRequest;
import com.example.kirana.dto.ReportResponse;

public interface ReportService {
    ReportResponse getSummary(ReportRequest request);
}
