package com.rideaustin.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.joda.money.CurrencyUnit;
import org.joda.money.Money;
import org.joda.money.MoneyUtils;
import org.springframework.beans.BeanUtils;

import com.rideaustin.Constants;
import com.rideaustin.model.ride.CityCarType;
import com.rideaustin.model.ride.FareDetails;
import com.rideaustin.model.ride.Ride;

public class FareUtils {

  private FareUtils() {
  }

  @Nonnull
  public static Money calculateCityFee(@Nonnull Money fare, @Nonnull CityCarType cityCarType) {
    return fare.multipliedBy(cityCarType.getCityFeeRate(), Constants.ROUNDING_MODE);
  }

  public static Money calculateProcessingFee(@Nullable CityCarType cityCarType) {
    return Optional.ofNullable(cityCarType)
      .map(CityCarType::getProcessingFeeRate)
      .map(v -> Money.of(CurrencyUnit.USD, v))
      .orElse(Money.of(CurrencyUnit.USD, BigDecimal.ONE));
  }

  public static Money estimateFare(CityCarType cityCarType, BigDecimal distance, BigDecimal time, BigDecimal surgeFactor,
    Money airportFee) {

    Money fare = estimateNormalFare(cityCarType, distance, time)
      .multipliedBy(surgeFactor, Constants.ROUNDING_MODE)
      .plus(cityCarType.getBookingFee().multipliedBy(surgeFactor, Constants.ROUNDING_MODE))
      .plus(airportFee);
    fare = fare.plus(calculateCityFee(fare, cityCarType));
    fare = fare.plus(calculateProcessingFee(cityCarType));
    return fare;
  }

  public static Money calculateNormalFare(FareDetails fareDetails) {
    return MoneyUtils.max(
      fareDetails.getMinimumFare(),

      fareDetails.getBaseFare()
        .plus(fareDetails.getTimeFare())
        .plus(fareDetails.getDistanceFare())
    );
  }

  /**
   * Return either cityCarType's minimalFare or minimal fare based on distance and time
   *
   * @param cityCarType
   * @param distance
   * @param time
   * @return
   */
  public static Money estimateNormalFare(CityCarType cityCarType, BigDecimal distance, BigDecimal time) {
    return MoneyUtils.max(
      cityCarType.getMinimumFare(),

      cityCarType.getBaseFare()
        .plus(cityCarType.getRatePerMile().multipliedBy(distance, Constants.ROUNDING_MODE))
        .plus(cityCarType.getRatePerMinute().multipliedBy(time, Constants.ROUNDING_MODE))
    );
  }

  public static Money calculateSurgeFare(FareDetails fareDetails, Ride ride) {
    Money subTotal = Optional.ofNullable(fareDetails.getSubTotal()).orElseGet(() -> calculateNormalFare(fareDetails));
    if (ride.getSurgeFactor() != null && ride.isSurgeRide()) {
      return subTotal.multipliedBy(ride.getSurgeFactor().subtract(Constants.NEUTRAL_SURGE_FACTOR), Constants.ROUNDING_MODE);
    }
    return Constants.ZERO_USD;
  }

  public static Money calculateSubTotal(FareDetails fareDetails) {
    return fareDetails.getNormalFare().plus(fareDetails.getSurgeFare());
  }

  public static Money calculateTotalFare(FareDetails fareDetails) {
    return MoneyUtils.max(Constants.ZERO_USD, fareDetails.getSubTotal()
      .minus(fareDetails.getFreeCreditCharged())
      .plus(fareDetails.getProcessingFee())
      .plus(fareDetails.getBookingFee())
      .plus(fareDetails.getAirportFee())
      .plus(fareDetails.getCityFee()));
  }

  public static Money calculateRoundUp(Money totalCharge) {
    Money newTotalFare = Money.of(CurrencyUnit.USD, totalCharge.getAmount().setScale(0, RoundingMode.UP));
    return newTotalFare.minus(totalCharge);
  }

  public static Money adjustStripeChargeAmount(Money charge) {
    if (charge.compareTo(Constants.MINIMUM_STRIPE_CHARGE) < 0) {
      return Constants.ZERO_USD;
    }
    return charge;
  }

  public static Money calculateRAFee(final CityCarType carType, Money subTotal) {
    Money minimumFare = carType.getMinimumFare();
    Money fixedRAFee = carType.getFixedRAFee();
    if (subTotal.isEqual(minimumFare)) {
      fixedRAFee = Constants.ZERO_USD;
    } else if (subTotal.minus(fixedRAFee).isLessThan(minimumFare)) {
      fixedRAFee = subTotal.minus(minimumFare);
    }
    return fixedRAFee;
  }

  public static Optional<FareDetails> setBaseRates(@Nonnull Ride ride, CityCarType cityCarType) {
    FareDetails fareDetails = new FareDetails();
    BeanUtils.copyProperties(ride.getFareDetails(), fareDetails);
    fareDetails.setMinimumFare(cityCarType.getMinimumFare());
    fareDetails.setBaseFare(cityCarType.getBaseFare());
    fareDetails.setBookingFee(cityCarType.getBookingFee().multipliedBy(ride.getSurgeFactor(), Constants.ROUNDING_MODE));
    fareDetails.setRatePerMile(cityCarType.getRatePerMile());
    fareDetails.setRatePerMinute(cityCarType.getRatePerMinute());
    return Optional.of(fareDetails);
  }

  public static Money calculateDistanceFare(final Money ratePerMile, BigDecimal milesTravelled) {
    return MoneyUtils.min(Constants.MAXIMUM_DISTANCE_FARE,
        ratePerMile.multipliedBy(milesTravelled, Constants.ROUNDING_MODE));
  }

  public static Money calculateTimeFareMillis(final Money ratePerMinute, BigDecimal rideDurationMillis) {
    return ratePerMinute
      .multipliedBy(Constants.MINUTES_PER_MILLISECOND.multiply(rideDurationMillis), Constants.ROUNDING_MODE);
  }

  public static Money calculateTimeFare(final Money ratePerMinute, BigDecimal rideDuration) {
    return ratePerMinute
      .multipliedBy(rideDuration, Constants.ROUNDING_MODE);
  }
}
