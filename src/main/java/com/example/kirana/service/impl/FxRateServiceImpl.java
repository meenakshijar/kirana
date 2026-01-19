package com.example.kirana.service.impl;

import com.example.kirana.service.FxRateService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;

@Service
public class FxRateServiceImpl implements FxRateService {

    private final StringRedisTemplate redisTemplate;

    public FxRateServiceImpl(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public BigDecimal getFxRate(String fromCurrency, String toCurrency) {

        if (fromCurrency == null || toCurrency == null) {
            throw new RuntimeException("Currency cannot be null");
        }

        String from = fromCurrency.toUpperCase();
        String to = toCurrency.toUpperCase();

        // ✅ Same currency means rate = 1
        if (from.equals(to)) {
            return BigDecimal.ONE;
        }

        String key = "fx:" + from + ":" + to;

        // ✅ 1) Check Redis cache
        String cachedRate = redisTemplate.opsForValue().get(key);
        if (cachedRate != null) {
            return new BigDecimal(cachedRate);
        }

        // ✅ 2) Call external FX API (placeholder for now)
        BigDecimal apiRate = callExternalFxApi(from, to);

        // ✅ 3) Cache it in Redis with TTL
        redisTemplate.opsForValue()
                .set(key, apiRate.toString(), Duration.ofMinutes(5));

        return apiRate;
    }

    private BigDecimal callExternalFxApi(String from, String to) {
        // TODO: Replace with real API call using RestTemplate/WebClient
        // Hard-coded fallback for now
        return new BigDecimal("83.25");
    }
}
