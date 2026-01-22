package com.example.kirana.controller;

import com.example.kirana.dto.ReportKafkaMessage;
import com.example.kirana.dto.ReportRequest;
import com.example.kirana.kafka.ReportProducer;
import jakarta.validation.Valid;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/report")
public class ReportController {

    private final ReportProducer reportProducer;

    public ReportController(ReportProducer reportProducer) {
        this.reportProducer = reportProducer;
    }

    // POST /report/summary (ASYNC)
    @PostMapping("/summary")
    public ResponseEntity<Map<String, Object>> requestReport(@Valid @RequestBody ReportRequest request) {

        String reportId = "RPT_" + UUID.randomUUID();

        ReportKafkaMessage message = new ReportKafkaMessage();
        message.setReportId(reportId);
        message.setStoreId(request.getStoreId());
        message.setPeriod(request.getPeriod());
        message.setPeriodTime(request.getPeriodTime());

        reportProducer.sendReportRequest(message);

        Map<String, Object> response = new HashMap<>();
        response.put("reportId", reportId);
        response.put("status", "PENDING");
        response.put("message", "Report request queued");

        return ResponseEntity.ok(response);
    }


    // GET /report/download/{reportId}
    @GetMapping("/download/{reportId}")
    public ResponseEntity<?> downloadReport(@PathVariable String reportId) {

        File file = new File("reports/" + reportId + ".csv");

        if (!file.exists()) {
            Map<String, Object> response = new HashMap<>();
            response.put("reportId", reportId);
            response.put("status", "PENDING");
            response.put("message", "Report not ready yet");
            return ResponseEntity.ok(response);
        }

        Resource resource = new FileSystemResource(file);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + reportId + ".csv\"")
                .header(HttpHeaders.CONTENT_TYPE, "text/csv")
                .body(resource);
    }
}
