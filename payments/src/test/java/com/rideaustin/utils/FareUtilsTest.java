package com.rideaustin.utils;

import static com.rideaustin.service.FareTestConstants.BASE_FARE;
import static com.rideaustin.service.FareTestConstants.BOOKING_FEE;
import static com.rideaustin.service.FareTestConstants.DISTANCE_FARE;
import static com.rideaustin.service.FareTestConstants.MINIMUM_FARE;
import static com.rideaustin.service.FareTestConstants.TIME_FARE;
import static com.rideaustin.service.FareTestConstants.money;
import static com.rideaustin.service.FareTestConstants.stubCityCarType;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.is;

import java.math.BigDecimal;

import org.joda.money.CurrencyUnit;
import org.joda.money.Money;
import org.junit.Test;

import com.rideaustin.Constants;
import com.rideaustin.model.ride.CityCarType;
import com.rideaustin.model.ride.FareDetails;
import com.rideaustin.model.ride.Ride;

public class FareUtilsTest {

  private static final CityCarType CITY_CAR_TYPE = stubCityCarType().get();

  @Test
  public void testCalculateCityFee() {
    //city fee = fare * 1%
    Money result = FareUtils.calculateCityFee(money(1), CITY_CAR_TYPE);

    assertThat(result.getAmount(), is(closeTo(BigDecimal.valueOf(0.01), BigDecimal.ZERO)));
  }

  @Test
  public void testCalculateProcessingFeeWithBigFare() {
    Money result = FareUtils.calculateProcessingFee(CITY_CAR_TYPE);

    assertThat(result.getAmount(), is(closeTo(BigDecimal.valueOf(1.0), BigDecimal.ZERO)));
  }

  @Test
  public void testCalculateProcessingFeeWithSmallFare() {
    Money result = FareUtils.calculateProcessingFee(CITY_CAR_TYPE);

    assertThat(result.getAmount(), is(closeTo(BigDecimal.valueOf(1), BigDecimal.ZERO)));
  }

  @Test
  public void testCalculateProcessingFeeWithoutCityCarType() {
    Money result = FareUtils.calculateProcessingFee(null);

    assertThat(result.getAmount(), is(closeTo(BigDecimal.valueOf(1), BigDecimal.ZERO)));
  }

  @Test
  public void testEstimateMinimumFareReturnsMinimumFareIfItsGreaterThanBase() {
    //distance rate = $1.5 / mi
    //time rate = $0.25 / minute
    //minimum fare = $5
    Money result = FareUtils.estimateNormalFare(CITY_CAR_TYPE, BigDecimal.ONE, BigDecimal.ONE);

    assertThat(result.getAmount(), is(closeTo(MINIMUM_FARE.getAmount(), BigDecimal.ZERO)));
  }

  @Test
  public void testEstimateMinimumFareReturnsBaseIfItsGreaterThanMinimum() {
    //base fare = $1.5
    //distance rate = $1.5 / mi
    //time rate = $0.25 / minute
    //minimum fare = $5
    Money result = FareUtils.estimateNormalFare(CITY_CAR_TYPE, BigDecimal.TEN, BigDecimal.TEN);

    assertThat(result.getAmount(), is(closeTo(BigDecimal.valueOf(19L), BigDecimal.ZERO)));
  }

  @Test
  public void testEstimateFare() {
    Money airportFee = Money.of(CurrencyUnit.USD, BigDecimal.TEN);
    BigDecimal surgeFactor = BigDecimal.valueOf(1.5);
    //minimum fare = $19
    //surge factor = 1.5
    //city fee = fare * 1%
    //booking fee = 1.5 * surge factor
    //processing fee = max(fare * processingFeeRate + processingFeeFixedPart, processingFeeMinimum)
    //processingFeeRate = 2.3%
    //fixedPart = $0.3
    //processingFeeMinimum = $1
    //airport fee = $10
    //fare estimate = minimum fare * surge factor + city fee + processing fee + airport fee ()

    Money result = FareUtils.estimateFare(CITY_CAR_TYPE, BigDecimal.TEN, BigDecimal.TEN, surgeFactor, airportFee);

    assertThat(result.getAmount(), is(closeTo(BigDecimal.valueOf(42.16), BigDecimal.ZERO)));
  }

  @Test
  public void testCalculateNormalFareReturnsMinimumFareIfItsGreaterThanBase() {
    //distance rate = $1.5 / mi
    //time rate = $0.25 / minute
    //minimum fare = $5
    FareDetails fareDetails = FareDetails.builder()
      .minimumFare(MINIMUM_FARE)
      .baseFare(BASE_FARE)
      .timeFare(TIME_FARE)
      .distanceFare(DISTANCE_FARE)
      .build();
    Money result = FareUtils.calculateNormalFare(fareDetails);

    assertThat(result.getAmount(), is(closeTo(MINIMUM_FARE.getAmount(), BigDecimal.ZERO)));
  }

  @Test
  public void testCalculateNormalFareReturnsBaseIfItsGreaterThanMinimum() {
    //distance rate = $1.5 / mi
    //time rate = $0.25 / minute
    //minimum fare = $5
    FareDetails fareDetails = FareDetails.builder()
      .minimumFare(MINIMUM_FARE)
      .baseFare(BASE_FARE)
      .timeFare(TIME_FARE)
      .distanceFare(DISTANCE_FARE.multipliedBy(5))
      .build();
    Money result = FareUtils.calculateNormalFare(fareDetails);

    assertThat(result.getAmount(), is(closeTo(BigDecimal.valueOf(10.5), BigDecimal.ZERO)));
  }

  @Test
  public void testCalculateSurgeFareReturnsZeroIfSurgeFactorNotSet() {
    FareDetails fareDetails = FareDetails.builder()
      .subTotal(money(10d))
      .bookingFee(BOOKING_FEE)
      .build();
    Ride ride = new Ride();

    Money surgeFare = FareUtils.calculateSurgeFare(fareDetails, ride);

    assertThat(surgeFare.getAmount(), is(closeTo(BigDecimal.ZERO, BigDecimal.ZERO)));
  }

  @Test
  public void testCalculateSurgeFareReturnsZeroIfRideIsNotUnderSurge() {
    FareDetails fareDetails = FareDetails.builder()
      .subTotal(money(10d))
      .bookingFee(BOOKING_FEE)
      .build();
    Ride ride = new Ride();
    ride.setSurgeFactor(BigDecimal.ONE);

    Money surgeFare = FareUtils.calculateSurgeFare(fareDetails, ride);

    assertThat(surgeFare.getAmount(), is(closeTo(BigDecimal.ZERO, BigDecimal.ZERO)));
  }

  @Test
  public void testCalculateSurgeFare() {
    FareDetails fareDetails = FareDetails.builder()
      .subTotal(MINIMUM_FARE)
      .minimumFare(MINIMUM_FARE)
      .baseFare(BASE_FARE)
      .timeFare(TIME_FARE)
      .distanceFare(DISTANCE_FARE)
      .bookingFee(BOOKING_FEE)
      .build();
    BigDecimal surgeFactor = BigDecimal.valueOf(1.5);
    Ride ride = new Ride();
    ride.setSurgeFactor(surgeFactor);

    Money surgeFare = FareUtils.calculateSurgeFare(fareDetails, ride);

    BigDecimal expected = FareUtils.calculateNormalFare(fareDetails)
      .multipliedBy(surgeFactor.subtract(BigDecimal.ONE), Constants.ROUNDING_MODE)
      .getAmount();
    assertThat(surgeFare.getAmount(), is(closeTo(expected, BigDecimal.ZERO)));
  }

  @Test
  public void testCalculateSubTotal() {
    FareDetails fareDetails = FareDetails.builder()
      .normalFare(money(10d))
      .surgeFare(money(5d))
      .build();

    Money result = FareUtils.calculateSubTotal(fareDetails);

    assertThat(result.getAmount(), is(closeTo(BigDecimal.valueOf(15), BigDecimal.ZERO)));
  }

  @Test
  public void testCalculateRoundUp() {
    Money totalCharge = money(10.5);

    Money roundUp = FareUtils.calculateRoundUp(totalCharge);

    assertThat(roundUp.getAmount(), is(closeTo(BigDecimal.valueOf(0.5), BigDecimal.ZERO)));
  }

  @Test
  public void testAdjustStripeChargeAmountReturnsZeroIfChargeIsLessThanMinimum() {
    Money charge = Constants.MINIMUM_STRIPE_CHARGE.minus(0.01);

    Money result = FareUtils.adjustStripeChargeAmount(charge);

    assertThat(result.getAmount(), is(closeTo(BigDecimal.ZERO, BigDecimal.ZERO)));
  }

  @Test
  public void testAdjustStripeChargeAmountReturnsChargeIfItsMoreThanMinimum() {
    Money charge = Constants.MINIMUM_STRIPE_CHARGE.plus(0.01);

    Money result = FareUtils.adjustStripeChargeAmount(charge);

    assertThat(result.getAmount(), is(closeTo(charge.getAmount(), BigDecimal.ZERO)));
  }

  @Test
  public void testCalculateRAFeeWhenSubTotalIsMinimumNoSurge() {
    Money result = FareUtils.calculateRAFee(CITY_CAR_TYPE, CITY_CAR_TYPE.getMinimumFare());

    assertThat(result.getAmount(), is(closeTo(BigDecimal.ZERO, BigDecimal.ZERO)));
  }

  @Test
  public void testCalculateRAFeeWhenSubtotalIsLessThanMinimumPlusFeeNoSurge() {
    Money result = FareUtils.calculateRAFee(CITY_CAR_TYPE, CITY_CAR_TYPE.getMinimumFare().plus(money(0.5)));

    assertThat(result.getAmount(), is(closeTo(BigDecimal.valueOf(0.5), BigDecimal.ZERO)));
  }

  @Test
  public void testCalculateRAFeeWhenSubtotalIsMoreThanMinimumNoSurge() {
    Money subtotal = CITY_CAR_TYPE.getMinimumFare().plus(money(5));

    Money result = FareUtils.calculateRAFee(CITY_CAR_TYPE, subtotal);

    assertThat(result.getAmount(), is(closeTo(CITY_CAR_TYPE.getFixedRAFee().getAmount(), BigDecimal.ZERO)));
  }

}