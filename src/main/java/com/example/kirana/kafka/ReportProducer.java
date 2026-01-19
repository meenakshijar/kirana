package com.example.kirana.kafka;

import com.example.kirana.dto.ReportKafkaMessage;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class ReportProducer {

    private final KafkaTemplate<String, ReportKafkaMessage> kafkaTemplate;

    public ReportProducer(KafkaTemplate<String, ReportKafkaMessage> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendReportRequest(ReportKafkaMessage message) {
        kafkaTemplate.send("report-requests", message.getReportId(), message);
    }
}
