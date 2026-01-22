package com.example.kirana.service.impl;

import com.example.kirana.service.FxRateService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Map;

@Service
public class FxRateServiceImpl implements FxRateService {

    private final StringRedisTemplate redisTemplate;
    private final RestTemplate restTemplate;

    public FxRateServiceImpl(StringRedisTemplate redisTemplate, RestTemplate restTemplate) {
        this.redisTemplate = redisTemplate;
        this.restTemplate = restTemplate;
    }

    @Override
    public BigDecimal getFxRate(String fromCurrency, String toCurrency) {

        if (fromCurrency == null || toCurrency == null) {
            throw new RuntimeException("Currency cannot be null");
        }

        String from = fromCurrency.toUpperCase();
        String to = toCurrency.toUpperCase();

        if (from.equals(to)) {
            return BigDecimal.ONE;
        }
        String key = "fx:" + from + ":" + to;

        String cachedRate = redisTemplate.opsForValue().get(key);
        if (cachedRate != null) {
            return new BigDecimal(cachedRate);
        }

        BigDecimal apiRate = callExternalFxApi(from, to);

        redisTemplate.opsForValue().set(key, apiRate.toString(), Duration.ofMinutes(5));

        return apiRate;
    }

    private BigDecimal callExternalFxApi(String from, String to) {

        String url = "https://api.fxratesapi.com/latest?base=" + from + "&symbols=" + to;

        Map response = restTemplate.getForObject(url, Map.class);

        if (response == null || !response.containsKey("rates")) {
            throw new RuntimeException("FX API invalid response");
        }

        Map<String, Object> rates = (Map<String, Object>) response.get("rates");

        Object rateObj = rates.get(to);
        if (rateObj == null) {
            throw new RuntimeException("FX rate not found for: " + from + " -> " + to);
        }

        return new BigDecimal(rateObj.toString());
    }
}
