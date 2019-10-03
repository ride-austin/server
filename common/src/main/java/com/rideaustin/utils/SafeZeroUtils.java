package com.rideaustin.utils;

import java.math.BigDecimal;
import java.util.Optional;

import org.joda.money.Money;

import com.querydsl.core.types.dsl.ComparablePath;
import com.querydsl.core.types.dsl.NumberExpression;
import com.rideaustin.Constants;

public class SafeZeroUtils {

  private SafeZeroUtils() {}

  public static Integer safeZero(Integer value) {
    return Optional.ofNullable(value).orElse(0);
  }

  public static double safeZero(Double value) {
    return Optional.ofNullable(value).orElse(0.0);
  }

  public static NumberExpression<BigDecimal> safeZero(ComparablePath<Money> money) {
    return money.coalesce(Constants.ZERO_USD).asNumber().castToNum(BigDecimal.class);
  }

  public static Long safeZero(Long value) {
    return Optional.ofNullable(value).orElse(0L);
  }

  public static BigDecimal safeZero(BigDecimal value) {
    return Optional.ofNullable(value).orElse(BigDecimal.ZERO);
  }

  public static Money safeZero(Money value) {
    return Optional.ofNullable(value).orElse(Constants.ZERO_USD);
  }

  public static BigDecimal safeZeroAmount(Money value) {
    return Optional.ofNullable(value).map(Money::getAmount).orElse(BigDecimal.ZERO);
  }
}
