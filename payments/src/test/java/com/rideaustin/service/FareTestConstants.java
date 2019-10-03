package com.rideaustin.service;

import java.math.BigDecimal;
import java.util.Optional;

import org.joda.money.CurrencyUnit;
import org.joda.money.Money;

import com.rideaustin.model.ride.CityCarType;

public final class FareTestConstants {

  public static final Money MINIMUM_FARE = money(5d);
  public static final Money RATE_PER_MILE = money(1.5d);
  public static final Money RATE_PER_MINUTE = money(0.25d);
  public static final Money BASE_FARE = money(1.5d);
  public static final Money BOOKING_FEE = money(1.5d);
  public static final Money FIXED_RA_FEE = money(0.99);
  public static final BigDecimal CITY_FEE_RATE = BigDecimal.valueOf(0.01);
  public static final BigDecimal PROCESSING_FEE_RATE = BigDecimal.valueOf(1.0);
  public static final Money TIME_FARE = money(1.5);
  public static final Money DISTANCE_FARE = money(1.5);
  public static final Money CANCELLATION_FEE = money(5);

  public static Money money(double amount) {
    return Money.of(CurrencyUnit.USD, BigDecimal.valueOf(amount));
  }

  public static Optional<CityCarType> stubCityCarType() {
    CityCarType carType = new CityCarType();
    carType.setMinimumFare(MINIMUM_FARE);
    carType.setRatePerMile(RATE_PER_MILE);
    carType.setRatePerMinute(RATE_PER_MINUTE);
    carType.setBaseFare(BASE_FARE);
    carType.setBookingFee(BOOKING_FEE);
    carType.setFixedRAFee(FIXED_RA_FEE);
    carType.setCityFeeRate(CITY_FEE_RATE);
    carType.setProcessingFeeRate(PROCESSING_FEE_RATE);
    carType.setCancellationFee(CANCELLATION_FEE);
    return Optional.of(carType);
  }
}
