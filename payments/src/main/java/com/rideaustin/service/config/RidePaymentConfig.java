package com.rideaustin.service.config;

import java.math.BigDecimal;

import javax.inject.Inject;

import org.joda.money.CurrencyUnit;
import org.joda.money.Money;
import org.springframework.stereotype.Component;

import com.rideaustin.clients.configuration.ConfigurationItemCache;
import com.rideaustin.filter.ClientType;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@Component
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class RidePaymentConfig {

  private static final String CONFIGURATION_KEY = "ridePayment";
  private static final String CANCELLATION_CHARGE_FREE_PERIOD_KEY = "cancellationChargeFreePeriod";
  private static final String TIP_LIMIT_KEY = "tipLimit";
  private static final String UPFRONT_ENABLED_KEY = "upfrontEnabled";
  private static final String UPFRONT_TIMEOUT_KEY = "upfrontTimeout";
  private static final String UPFRONT_THRESHOLD_KEY = "upfrontThreshold";
  private static final String ASYNC_PREAUTH_ENABLED_KEY = "asyncPreauthEnabled";

  private final ConfigurationItemCache cache;

  public int getCancellationChargeFreePeriod() {
    return cache.getConfigAsInt(ClientType.CONSOLE, CONFIGURATION_KEY, CANCELLATION_CHARGE_FREE_PERIOD_KEY);
  }

  public BigDecimal getTipLimit() {
    return BigDecimal.valueOf(cache.getConfigAsDouble(ClientType.CONSOLE, CONFIGURATION_KEY, TIP_LIMIT_KEY));
  }

  public boolean isUpfrontPricingEnabled() {
    return cache.getConfigAsBoolean(ClientType.CONSOLE, CONFIGURATION_KEY, UPFRONT_ENABLED_KEY, null);
  }

  public boolean isAsyncPreauthEnabled() {
    return cache.getConfigAsBoolean(ClientType.CONSOLE, CONFIGURATION_KEY, ASYNC_PREAUTH_ENABLED_KEY, null);
  }

  public int getUpfrontPricingTimeout() {
    return cache.getConfigAsInt(ClientType.CONSOLE, CONFIGURATION_KEY, UPFRONT_TIMEOUT_KEY);
  }

  public Money getUpfrontRecalculationThreshold() {
    return Money.of(CurrencyUnit.USD, cache.getConfigAsDouble(ClientType.CONSOLE, CONFIGURATION_KEY, UPFRONT_THRESHOLD_KEY));
  }

}
