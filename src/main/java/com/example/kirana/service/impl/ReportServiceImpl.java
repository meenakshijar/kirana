package com.example.kirana.service.impl;

import com.example.kirana.dao.PurchaseLineItemsDao;
import com.example.kirana.dao.TransactionLineItemsDao;
import com.example.kirana.dto.ReportRequest;
import com.example.kirana.dto.ReportResponse;
import com.example.kirana.lock.RedissonLockService;
import com.example.kirana.model.TransactionType;
import com.example.kirana.model.mongo.Store;
import com.example.kirana.repository.mongo.StoreRepository;
import com.example.kirana.security.StoreAccessValidator;
import com.example.kirana.service.ReportService;
import org.redisson.api.RLock;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * The type Report service.
 */
@Service
public class ReportServiceImpl implements ReportService {

    private final TransactionLineItemsDao transactionLineItemsDao;
    private final PurchaseLineItemsDao purchaseLineItemsDao;
    private final StoreRepository storeRepository;
    private final RedissonLockService redissonLockService;
    private final StoreAccessValidator storeAccessValidator;


    /**
     * Instantiates a new Report service.
     *
     * @param transactionLineItemsDao the transaction line items dao
     * @param purchaseLineItemsDao    the purchase line items dao
     * @param storeRepository         the store repository
     * @param redissonLockService     the redisson lock service
     * @param storeAccessValidator    the store access validator
     */
    public ReportServiceImpl(TransactionLineItemsDao transactionLineItemsDao,
                             PurchaseLineItemsDao purchaseLineItemsDao,
                             StoreRepository storeRepository,
                             RedissonLockService redissonLockService, StoreAccessValidator storeAccessValidator) {
        this.transactionLineItemsDao = transactionLineItemsDao;
        this.purchaseLineItemsDao = purchaseLineItemsDao;
        this.storeRepository = storeRepository;
        this.redissonLockService = redissonLockService;
        this.storeAccessValidator = storeAccessValidator;
    }

    @Override
    public ReportResponse getSummary(ReportRequest request) {

        Store store = storeRepository.findById(request.getStoreId())
                .orElseThrow(() -> new RuntimeException("Store not found: " + request.getStoreId()));

        String baseCurrency = store.getBaseCurrency();
        if (baseCurrency == null || baseCurrency.isBlank()) {
            throw new RuntimeException("Base currency not configured for store: " + request.getStoreId());
        }

        String lockKey = "lock:report:store:" + request.getStoreId() + ":" + request.getPeriod();
        RLock lock = redissonLockService.lock(lockKey, 2, 15);

        if (lock == null) {
            throw new RuntimeException("Another report is already processing for this store. Try again.");
        }

        try {
            LocalDateTime start;
            LocalDateTime end;

            int year = request.getPeriodTime().getYear();
            String periodLabel;

            switch (request.getPeriod()) {

                case "YEARLY" -> {
                    start = LocalDate.of(year, 1, 1).atStartOfDay();
                    end = LocalDate.of(year, 12, 31).atTime(23, 59, 59);
                    periodLabel = String.valueOf(year);
                }

                case "MONTHLY" -> {
                    int month = request.getPeriodTime().getMonth();
                    start = LocalDate.of(year, month, 1).atStartOfDay();
                    end = start.plusMonths(1).minusSeconds(1);
                    periodLabel = year + "-" + String.format("%02d", month);
                }

                case "WEEKLY" -> {
                    int week = request.getPeriodTime().getWeek();

                    LocalDate firstDay = LocalDate.of(year, 1, 1);
                    LocalDate weekStart = firstDay.plusWeeks(week - 1);

                    start = weekStart.atStartOfDay();
                    end = weekStart.plusDays(6).atTime(23, 59, 59);

                    periodLabel = year + "-W" + String.format("%02d", week);
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
            response.setPeriodLabel(periodLabel);
            response.setBaseCurrency(baseCurrency);
            response.setTotalCredit(totalCredit);
            response.setTotalDebit(totalDebit);
            response.setNetFlow(netFlow);
            response.setGeneratedAt(LocalDateTime.now());

            return response;

        } finally {
            redissonLockService.unlock(lock);
        }
    }
}
