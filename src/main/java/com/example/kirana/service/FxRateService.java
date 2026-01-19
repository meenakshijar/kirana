package com.example.kirana.service;

import java.math.BigDecimal;

public interface FxRateService {

    BigDecimal getFxRate(String fromCurrency, String toCurrency);
}
