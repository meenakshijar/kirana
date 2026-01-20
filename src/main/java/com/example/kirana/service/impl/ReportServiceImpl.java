package com.example.kirana.service.impl;

import com.example.kirana.dao.PurchaseLineItemsDao;
import com.example.kirana.dao.TransactionLineItemsDao;
import com.example.kirana.dto.ReportRequest;
import com.example.kirana.dto.ReportResponse;
import com.example.kirana.model.TransactionType;
import com.example.kirana.service.ReportService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
public class ReportServiceImpl implements ReportService {

    private final TransactionLineItemsDao transactionLineItemsDao;
    private final PurchaseLineItemsDao purchaseLineItemsDao;

    public ReportServiceImpl(
            TransactionLineItemsDao transactionLineItemsDao,
            PurchaseLineItemsDao purchaseLineItemsDao
    ) {
        this.transactionLineItemsDao = transactionLineItemsDao;
        this.purchaseLineItemsDao = purchaseLineItemsDao;
    }

    @Override
    public ReportResponse getSummary(ReportRequest request) {


        String baseCurrency = "INR";

        LocalDateTime start;
        LocalDateTime end;

        int year = request.getPeriodTime().getYear();

        switch (request.getPeriod()) {
            case "YEARLY" -> {
                start = LocalDate.of(year, 1, 1).atStartOfDay();
                end = LocalDate.of(year, 12, 31).atTime(23, 59, 59);
            }

            case "MONTHLY" -> {
                int month = request.getPeriodTime().getMonth();
                start = LocalDate.of(year, month, 1).atStartOfDay();
                end = start.plusMonths(1).minusSeconds(1);
            }

            case "WEEKLY" -> {
                // Basic assumption: week = 1..52, you can refine later
                int week = request.getPeriodTime().getWeek();

                LocalDate firstDay = LocalDate.of(year, 1, 1);
                LocalDate weekStart = firstDay.plusWeeks(week - 1);

                start = weekStart.atStartOfDay();
                end = weekStart.plusDays(6).atTime(23, 59, 59);
            }

            default -> throw new RuntimeException("Invalid period: " + request.getPeriod());
        }

        BigDecimal totalCredit = transactionLineItemsDao
                .sumTotalByStoreAndTypeAndCreatedAtBetween(
                        request.getStoreId(),
                        TransactionType.CREDIT,
                        start,
                        end
                );

        BigDecimal totalDebit = purchaseLineItemsDao
                .sumTotalByStoreAndTypeAndCreatedAtBetween(
                        request.getStoreId(),
                        TransactionType.DEBIT,
                        start,
                        end
                );

        BigDecimal netFlow = totalCredit.subtract(totalDebit);

        ReportResponse response = new ReportResponse();
        response.setStoreId(request.getStoreId());
        response.setPeriod(request.getPeriod());
        response.setPeriodLabel(request.getPeriod() + " " + year);
        response.setBaseCurrency(baseCurrency);
        response.setTotalCredit(totalCredit);
        response.setTotalDebit(totalDebit);
        response.setNetFlow(netFlow);
        response.setGeneratedAt(LocalDateTime.now());

        return response;
    }
}
