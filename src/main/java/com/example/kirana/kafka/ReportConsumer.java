package com.example.kirana.kafka;

import com.example.kirana.dao.PurchaseLineItemsDao;
import com.example.kirana.dao.TransactionLineItemsDao;
import com.example.kirana.dto.ReportKafkaMessage;
import com.example.kirana.model.TransactionType;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileWriter;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
public class ReportConsumer {

    private final TransactionLineItemsDao transactionLineItemsDao;
    private final PurchaseLineItemsDao purchaseLineItemsDao;

    public ReportConsumer(
            TransactionLineItemsDao transactionLineItemsDao,
            PurchaseLineItemsDao purchaseLineItemsDao
    ) {
        this.transactionLineItemsDao = transactionLineItemsDao;
        this.purchaseLineItemsDao = purchaseLineItemsDao;
    }

    @KafkaListener(topics = "report-requests", groupId = "report-group")
    public void consumeReportRequest(ReportKafkaMessage message) {
        System.out.println("🔥 CONSUMER RECEIVED MESSAGE: " + message.getReportId());
        try {
            String reportId = message.getReportId();
            String storeId = message.getStoreId();
            String period = message.getPeriod();

            LocalDateTime start;
            LocalDateTime end;

            int year = message.getPeriodTime().getYear();

            switch (period) {
                case "YEARLY" -> {
                    start = LocalDate.of(year, 1, 1).atStartOfDay();
                    end = LocalDate.of(year, 12, 31).atTime(23, 59, 59);
                }
                case "MONTHLY" -> {
                    int month = message.getPeriodTime().getMonth();
                    start = LocalDate.of(year, month, 1).atStartOfDay();
                    end = start.plusMonths(1).minusSeconds(1);
                }
                case "WEEKLY" -> {
                    int week = message.getPeriodTime().getWeek();
                    LocalDate firstDay = LocalDate.of(year, 1, 1);
                    LocalDate weekStart = firstDay.plusWeeks(week - 1);

                    start = weekStart.atStartOfDay();
                    end = weekStart.plusDays(6).atTime(23, 59, 59);
                }
                default -> throw new RuntimeException("Invalid period: " + period);
            }

            BigDecimal totalCredit = transactionLineItemsDao.sumTotalByStoreAndTypeAndCreatedAtBetween(
                    storeId,
                    TransactionType.CREDIT,
                    start,
                    end
            );

            BigDecimal totalDebit = purchaseLineItemsDao.sumTotalByStoreAndTypeAndCreatedAtBetween(
                    storeId,
                    TransactionType.DEBIT,
                    start,
                    end
            );

            BigDecimal netFlow = totalCredit.subtract(totalDebit);

            // ✅ Ensure reports folder exists
            File dir = new File("reports");
            if (!dir.exists()) {
                dir.mkdir();
            }

            // ✅ Create CSV file
            File csvFile = new File("reports/" + reportId + ".csv");

            try (FileWriter writer = new FileWriter(csvFile)) {

                writer.write("reportId,storeId,period,totalCredit,totalDebit,netFlow,generatedAt\n");
                writer.write(reportId + "," + storeId + "," + period + ","
                        + totalCredit + "," + totalDebit + "," + netFlow + ","
                        + LocalDateTime.now() + "\n");
            }

            System.out.println("✅ Report generated: " + csvFile.getAbsolutePath());

        } catch (Exception e) {
            System.out.println("❌ Report generation failed: " + e.getMessage());
        }
    }
}
