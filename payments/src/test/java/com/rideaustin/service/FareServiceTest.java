package com.rideaustin.service;

import static com.rideaustin.service.FareTestConstants.BASE_FARE;
import static com.rideaustin.service.FareTestConstants.BOOKING_FEE;
import static com.rideaustin.service.FareTestConstants.CANCELLATION_FEE;
import static com.rideaustin.service.FareTestConstants.MINIMUM_FARE;
import static com.rideaustin.service.FareTestConstants.RATE_PER_MILE;
import static com.rideaustin.service.FareTestConstants.RATE_PER_MINUTE;
import static com.rideaustin.service.FareTestConstants.money;
import static com.rideaustin.service.FareTestConstants.stubCityCarType;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.closeTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.joda.money.Money;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.core.env.Environment;
import org.springframework.statemachine.StateMachinePersist;

import com.google.maps.model.LatLng;
import com.rideaustin.Constants;
import com.rideaustin.model.Charity;
import com.rideaustin.model.enums.RideStatus;
import com.rideaustin.model.fee.SpecialFee;
import com.rideaustin.model.ride.CarType;
import com.rideaustin.model.ride.CityCarType;
import com.rideaustin.model.ride.FareDetails;
import com.rideaustin.model.ride.Ride;
import com.rideaustin.repo.dsl.RideDslRepository;
import com.rideaustin.service.airport.AirportService;
import com.rideaustin.service.config.RidePaymentConfig;
import com.rideaustin.service.model.Events;
import com.rideaustin.service.model.States;
import com.rideaustin.service.promocodes.PromocodeService;
import com.rideaustin.service.promocodes.PromocodeUseRequest;
import com.rideaustin.service.promocodes.PromocodeUseResult;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RunWith(MockitoJUnitRunner.class)
public class FareServiceTest {

  @Mock
  private CarTypeService carTypeService;
  @Mock
  private AirportService airportService;
  @Mock
  private RidePaymentConfig config;
  @Mock
  private PromocodeService promocodeService;
  @Mock
  private RideDslRepository rideDslRepository;
  @Mock
  private Environment environment;
  @Mock
  private StateMachinePersist<States, Events, String> contextAccess;
  @Mock
  private CampaignService campaignService;

  private FareService fareService;

  private LatLng pickupLatLng = new LatLng(10d, 10d);
  private LatLng dropoffLatLng = new LatLng(20d, 20d);

  private Ride ride = new Ride();

  @Before
  public void setup() throws Exception {
    FareServiceTestUtils.prepareRide(ride, pickupLatLng, dropoffLatLng);

    when(carTypeService.getCityCarTypeWithFallback(any(CarType.class), any())).thenReturn(stubCityCarType());
    when(carTypeService.getCityCarType(anyString(), anyLong())).thenReturn(stubCityCarType());
    when(campaignService.findMatchingCampaignForRide(ride)).thenReturn(Optional.empty());
    when(campaignService.findEligibleCampaign(any(Date.class), any(), any(), any(), any(), any())).thenReturn(Optional.empty());
    when(promocodeService.usePromocode(any(PromocodeUseRequest.class), eq(true))).thenReturn(new PromocodeUseResult());
    FareServiceTestUtils.setupAirport(airportService);

    fareService = new FareService(carTypeService, airportService, promocodeService,
      campaignService, config, rideDslRepository, environment, contextAccess);
  }

  @Test
  public void testCalculateTotalFareReturnsEmptyOnEmptyCarType() {
    when(carTypeService.getCityCarTypeWithFallback(any(CarType.class), anyLong())).thenReturn(Optional.empty());

    Optional<FareDetails> result = fareService.calculateTotalFare(ride, null);

    assertFalse(result.isPresent());
  }

  @Test
  public void testCalculateTotalFare() {
    FareDetails expected = expectedCompletedFareDetails();

    Optional<FareDetails> fareDetails = fareService.calculateTotalFare(ride, null);

    assertFareDetails(expected, fareDetails);
  }

  @Test
  public void testCalculateTotalFareWithVariableRAFee() {
    FareDetails expected = FareDetails.builder()
      .ratePerMile(RATE_PER_MILE)
      .ratePerMinute(RATE_PER_MINUTE)
      .minimumFare(MINIMUM_FARE)
      .baseFare(BASE_FARE)
      .bookingFee(BOOKING_FEE)
      .distanceFare(money(1.03))
      .timeFare(money(2.5))
      .cityFee(money(0.07))
      .processingFee(money(1))
      .subTotal(money(5.03))
      .normalFare(money(5.03))
      .surgeFare(money(0))
      .totalFare(money(7.6))
      .driverPayment(money(5.0))
      .raPayment(money(0.03))
      .roundUpAmount(money(0.0))
      .airportFee(money(0.0))
      .freeCreditCharged(money(0.0))
      .build();

    ride.setDistanceTravelled(new BigDecimal(1100));
    ride.setStartedOn(new DateTime().minusMinutes(10).toDate());

    Optional<FareDetails> fareDetails = fareService.calculateTotalFare(ride, null);

    assertFareDetails(expected, fareDetails);
  }

  @Test
  public void testCalculateTotalFareWithSurge() {
    FareDetails expected = FareDetails.builder()
      .ratePerMile(RATE_PER_MILE)
      .ratePerMinute(RATE_PER_MINUTE)
      .minimumFare(MINIMUM_FARE)
      .baseFare(BASE_FARE)
      .bookingFee(BOOKING_FEE.multipliedBy(1.5, Constants.ROUNDING_MODE))
      .distanceFare(money(0.93))
      .timeFare(money(75.0))
      .cityFee(money(1.18))
      .processingFee(money(1))
      .subTotal(money(116.15))
      .normalFare(money(77.43))
      .surgeFare(money(38.72))
      .totalFare(money(120.58))
      .driverPayment(money(115.16))
      .raPayment(money(0.99))
      .roundUpAmount(money(0.0))
      .airportFee(money(0.0))
      .freeCreditCharged(money(0.0))
      .build();
    setupSurge(BigDecimal.valueOf(1.5));

    Optional<FareDetails> fareDetails = fareService.calculateTotalFare(ride, null);

    assertFareDetails(expected, fareDetails);
  }

  @Test
  public void testCalculateTotalFareWithAirportPickupFee() {
    testCalculateTotalFareWithAirportFee(pickupLatLng);
  }

  @Test
  public void testCalculateTotalFareWithAirportDropoffFee() {
    testCalculateTotalFareWithAirportFee(dropoffLatLng);
  }

  @Test
  public void testCalculateTotalFareWithPromocode() {
    FareDetails expected = FareDetails.builder()
      .ratePerMile(RATE_PER_MILE)
      .ratePerMinute(RATE_PER_MINUTE)
      .minimumFare(MINIMUM_FARE)
      .baseFare(BASE_FARE)
      .bookingFee(BOOKING_FEE)
      .distanceFare(money(0.93))
      .timeFare(money(75.0))
      .cityFee(money(0.79))
      .processingFee(money(1))
      .subTotal(money(77.43))
      .normalFare(money(77.43))
      .surgeFare(money(0.0))
      .totalFare(money(60.72))
      .driverPayment(money(76.44))
      .raPayment(money(0.99))
      .roundUpAmount(money(0.0))
      .airportFee(money(0.0))
      .freeCreditCharged(money(20.0))
      .build();

    Optional<FareDetails> fareDetails = fareService.calculateTotalFare(ride, new PromocodeUseResult(BigDecimal.valueOf(20.0)));

    assertFareDetails(expected, fareDetails);
  }

  @Test
  public void testCalculateTotalFareWithPromocodeFull() {
    FareDetails expected = FareDetails.builder()
      .ratePerMile(RATE_PER_MILE)
      .ratePerMinute(RATE_PER_MINUTE)
      .minimumFare(MINIMUM_FARE)
      .baseFare(BASE_FARE)
      .bookingFee(BOOKING_FEE)
      .distanceFare(money(0.93))
      .timeFare(money(75.0))
      .cityFee(money(0.79))
      .processingFee(money(1.0))
      .subTotal(money(77.43))
      .normalFare(money(77.43))
      .surgeFare(money(0.0))
      .totalFare(money(3.29))
      .driverPayment(money(76.44))
      .raPayment(money(0.99))
      .roundUpAmount(money(0.0))
      .airportFee(money(0.0))
      .freeCreditCharged(money(77.43))
      .build();

    Optional<FareDetails> fareDetails = fareService.calculateTotalFare(ride, new PromocodeUseResult(BigDecimal.valueOf(77.43)));

    assertFareDetails(expected, fareDetails);
  }

  @Test
  public void testCalculateTotalFareWithCharity() {
    FareDetails expected = FareDetails.builder()
      .ratePerMile(RATE_PER_MILE)
      .ratePerMinute(RATE_PER_MINUTE)
      .minimumFare(MINIMUM_FARE)
      .baseFare(BASE_FARE)
      .bookingFee(BOOKING_FEE)
      .distanceFare(money(0.93))
      .timeFare(money(75.0))
      .cityFee(money(0.79))
      .processingFee(money(1))
      .subTotal(money(77.43))
      .normalFare(money(77.43))
      .surgeFare(money(0.0))
      .totalFare(money(80.72))
      .driverPayment(money(76.44))
      .raPayment(money(0.99))
      .roundUpAmount(money(0.28))
      .airportFee(money(0.0))
      .freeCreditCharged(money(0.0))
      .build();
    ride.getRider().setCharity(new Charity());
    when(rideDslRepository.findCharity(ride)).thenReturn(new Charity());

    Optional<FareDetails> fareDetails = fareService.calculateTotalFare(ride, null);

    assertFareDetails(expected, fareDetails);
  }

  @Test
  public void testCalculateTotalFareWithCharityPromocodeAirportAndSurge() {
    FareDetails expected = FareDetails.builder()
      .ratePerMile(RATE_PER_MILE)
      .ratePerMinute(RATE_PER_MINUTE)
      .minimumFare(MINIMUM_FARE)
      .baseFare(BASE_FARE)
      .bookingFee(BOOKING_FEE.multipliedBy(1.5, Constants.ROUNDING_MODE))
      .distanceFare(money(0.93))
      .timeFare(money(75.0))
      .cityFee(money(1.21))
      .processingFee(money(1))
      .subTotal(money(116.15))
      .normalFare(money(77.43))
      .surgeFare(money(38.72))
      .totalFare(money(103.61))
      .driverPayment(money(115.16))
      .raPayment(money(0.99))
      .roundUpAmount(money(0.39))
      .airportFee(money(FareServiceTestUtils.AIRPORT_FEE))
      .freeCreditCharged(money(20.0))
      .build();
    ride.getRider().setCharity(new Charity());
    FareServiceTestUtils.setupAirport(true, pickupLatLng, airportService);
    setupSurge(BigDecimal.valueOf(1.5));
    when(rideDslRepository.findCharity(ride)).thenReturn(new Charity());

    Optional<FareDetails> fareDetails = fareService.calculateTotalFare(ride, new PromocodeUseResult(BigDecimal.valueOf(20.0)));

    assertFareDetails(expected, fareDetails);
  }

  @Test
  public void testCalculateTotalFareWithCharityPromocodeAirportAndSurgeThreeTimes() {
    FareDetails expected = FareDetails.builder()
      .ratePerMile(RATE_PER_MILE)
      .ratePerMinute(RATE_PER_MINUTE)
      .minimumFare(MINIMUM_FARE)
      .baseFare(BASE_FARE)
      .bookingFee(BOOKING_FEE.multipliedBy(1.5, Constants.ROUNDING_MODE))
      .distanceFare(money(0.93))
      .timeFare(money(75.0))
      .cityFee(money(1.21))
      .processingFee(money(1))
      .subTotal(money(116.15))
      .normalFare(money(77.43))
      .surgeFare(money(38.72))
      .totalFare(money(103.61))
      .driverPayment(money(115.16))
      .raPayment(money(0.99))
      .roundUpAmount(money(0.39))
      .airportFee(money(FareServiceTestUtils.AIRPORT_FEE))
      .freeCreditCharged(money(20.0))
      .build();
    ride.getRider().setCharity(new Charity());
    FareServiceTestUtils.setupAirport(true, pickupLatLng, airportService);
    setupSurge(BigDecimal.valueOf(1.5));
    when(rideDslRepository.findCharity(ride)).thenReturn(new Charity());

    Optional<FareDetails> fareDetails = fareService.calculateTotalFare(ride, new PromocodeUseResult(BigDecimal.valueOf(20.0)));
    ride.setFareDetails(fareDetails.orElse(new FareDetails()));
    fareDetails = fareService.calculateTotalFare(ride, new PromocodeUseResult(BigDecimal.valueOf(20.0)));
    ride.setFareDetails(fareDetails.orElse(new FareDetails()));
    fareDetails = fareService.calculateTotalFare(ride, new PromocodeUseResult(BigDecimal.valueOf(20.0)));

    assertFareDetails(expected, fareDetails);
  }

  @Test
  public void testProcessCancellationReturnsEmptyOnEmptyCarType() {
    when(carTypeService.getCityCarTypeWithFallback(any(CarType.class), any())).thenReturn(Optional.empty());

    Optional<FareDetails> result = fareService.processCancellation(ride, true);

    assertFalse(result.isPresent());
  }

  @Test
  public void testProcessCancellation() {
    FareDetails expected = expectedCancelledFareDetails();

    Optional<FareDetails> fareDetails = fareService.processCancellation(ride, true);

    assertFareDetails(expected, fareDetails);
  }

  @Test
  public void testProcessCancellationWithoutCancellationFee() {
    FareDetails expected = new FareDetails();
    expected.reset();

    Optional<FareDetails> fareDetails = fareService.processCancellation(ride, false);

    assertFareDetails(expected, fareDetails);
  }

  @Test
  public void testCalculateFinalFareForCancelledRideWhenCancellationNotPassed() {
    FareDetails expected = expectedCancelledFareDetails();
    expected.setStripeCreditCharge(CANCELLATION_FEE);
    ride.setStatus(RideStatus.RIDER_CANCELLED);
    ride.setDriverAcceptedOn(new Date());
    ride.setRequestedOn(new Date());
    CityCarType cityCarType = CityCarType.builder().cancellationFee(CANCELLATION_FEE).build();
    when(carTypeService.getCityCarType(anyString(), anyLong()))
      .thenReturn(Optional.of(cityCarType));
    when(config.getCancellationChargeFreePeriod()).thenReturn(-60);

    Optional<FareDetails> fareDetails = fareService.calculateFinalFare(ride, null);

    assertFareDetails(expected, fareDetails);
  }

  @Test
  public void testCalculateFinalFareForCompletedRideWithoutTips() {
    FareDetails expected = expectedCompletedFareDetails();
    expected.setStripeCreditCharge(expected.getTotalCharge());
    ride.setStatus(RideStatus.COMPLETED);

    Optional<FareDetails> fareDetails = fareService.calculateFinalFare(ride, null);

    assertFareDetails(expected, fareDetails);
  }

  @Test
  public void testCalculateFinalFareForCompletedRideWithTips() {
    Money tip = money(5);
    FareDetails expected = expectedCompletedFareDetails();
    expected.setTip(tip);
    expected.setDriverPayment(expected.getDriverPayment().plus(tip));
    expected.setStripeCreditCharge(expected.getTotalCharge());
    ride.setStatus(RideStatus.COMPLETED);
    ride.getFareDetails().setTip(tip);

    Optional<FareDetails> fareDetails = fareService.calculateFinalFare(ride, null);

    assertFareDetails(expected, fareDetails);
  }

  @Test
  public void testGetSpecialFees() {
    FareServiceTestUtils.setupAirport(true, pickupLatLng, airportService);

    List<SpecialFee> fees = fareService.getSpecialFees(pickupLatLng);

    assertEquals(1, fees.size());
    assertThat(fees.get(0).getValue(), is(closeTo(BigDecimal.valueOf(FareServiceTestUtils.AIRPORT_FEE), BigDecimal.ZERO)));
  }

  @Test
  public void testGetSpecialFeesNoAirport() {
    FareServiceTestUtils.setupAirport(airportService);

    List<SpecialFee> fees = fareService.getSpecialFees(new LatLng(1d, 1d));

    assertEquals(0, fees.size());
  }

  @Test
  public void testCalculateTotalFareWithZeroRAFee() {

    ride.setCompletedOn(new Date(Instant.ofEpochMilli(ride.getStartedOn().getTime()).plus(1, ChronoUnit.MINUTES).toEpochMilli()));

    FareDetails expected = FareDetails.builder()
      .ratePerMile(RATE_PER_MILE)
      .ratePerMinute(RATE_PER_MINUTE)
      .minimumFare(MINIMUM_FARE)
      .baseFare(BASE_FARE)
      .bookingFee(BOOKING_FEE)
      .distanceFare(money(0.93))
      .timeFare(money(0.25))
      .cityFee(money(0.06))
      .processingFee(money(1))
      .subTotal(MINIMUM_FARE)
      .normalFare(MINIMUM_FARE)
      .surgeFare(money(0.0))
      .totalFare(money(7.56))
      .driverPayment(MINIMUM_FARE)
      .raPayment(money(0.0))
      .roundUpAmount(money(0.0))
      .airportFee(money(0.0))
      .freeCreditCharged(money(0.0))
      .build();

    Optional<FareDetails> actual = fareService.calculateTotalFare(ride, null);

    assertFareDetails(expected, actual);
  }

  private void testCalculateTotalFareWithAirportFee(LatLng airportLocation) {
    FareDetails expected = FareDetails.builder()
      .ratePerMile(RATE_PER_MILE)
      .ratePerMinute(RATE_PER_MINUTE)
      .minimumFare(MINIMUM_FARE)
      .baseFare(BASE_FARE)
      .bookingFee(BOOKING_FEE)
      .distanceFare(money(0.93))
      .timeFare(money(75.0))
      .cityFee(money(0.82))
      .processingFee(money(1))
      .subTotal(money(77.43))
      .normalFare(money(77.43))
      .surgeFare(money(0.0))
      .totalFare(money(83.75))
      .driverPayment(money(76.44))
      .raPayment(money(0.99))
      .roundUpAmount(money(0.0))
      .airportFee(money(FareServiceTestUtils.AIRPORT_FEE))
      .freeCreditCharged(money(0.0))
      .build();
    FareServiceTestUtils.setupAirport(true, airportLocation, airportService);

    Optional<FareDetails> fareDetails = fareService.calculateTotalFare(ride, null);

    assertFareDetails(expected, fareDetails);
  }

  private void assertFareDetails(FareDetails expected, Optional<FareDetails> fareDetails) {
    assertTrue(fareDetails.isPresent());
    assertEquals(expected, fareDetails.get());
  }

  private FareDetails expectedCancelledFareDetails() {
    return FareDetails.builder()
      .airportFee(Constants.ZERO_USD)
      .baseFare(Constants.ZERO_USD)
      .bookingFee(Constants.ZERO_USD)
      .cityFee(Constants.ZERO_USD)
      .distanceFare(Constants.ZERO_USD)
      .freeCreditCharged(Constants.ZERO_USD)
      .minimumFare(Constants.ZERO_USD)
      .normalFare(Constants.ZERO_USD)
      .processingFee(Constants.ZERO_USD)
      .raPayment(Constants.ZERO_USD)
      .ratePerMile(Constants.ZERO_USD)
      .ratePerMinute(Constants.ZERO_USD)
      .roundUpAmount(Constants.ZERO_USD)
      .stripeCreditCharge(Constants.ZERO_USD)
      .surgeFare(Constants.ZERO_USD)
      .timeFare(Constants.ZERO_USD)
      .tip(Constants.ZERO_USD)
      .subTotal(CANCELLATION_FEE)
      .driverPayment(CANCELLATION_FEE)
      .totalFare(CANCELLATION_FEE)
      .cancellationFee(CANCELLATION_FEE)
      .build();
  }

  private FareDetails expectedCompletedFareDetails() {
    return FareDetails.builder()
      .ratePerMile(RATE_PER_MILE)
      .ratePerMinute(RATE_PER_MINUTE)
      .minimumFare(MINIMUM_FARE)
      .baseFare(BASE_FARE)
      .bookingFee(BOOKING_FEE)
      .distanceFare(money(0.93))
      .timeFare(money(75.0))
      .cityFee(money(0.79))
      .processingFee(money(1))
      .subTotal(money(77.43))
      .normalFare(money(77.43))
      .surgeFare(money(0.0))
      .totalFare(money(80.72))
      .driverPayment(money(76.44))
      .raPayment(money(0.99))
      .roundUpAmount(money(0.0))
      .airportFee(money(0.0))
      .freeCreditCharged(money(0.0))
      .build();
  }

  private void setupSurge(BigDecimal surgeFactor) {
    ride.setSurgeFactor(surgeFactor);
  }

}