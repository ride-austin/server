package com.rideaustin.service;

import static com.rideaustin.test.util.TestUtils.money;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.startsWith;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Date;
import java.util.EnumSet;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.rideaustin.model.enums.PaymentStatus;
import com.rideaustin.model.enums.RideStatus;
import com.rideaustin.model.ride.ActiveDriver;
import com.rideaustin.model.ride.Ride;
import com.rideaustin.model.user.Driver;
import com.rideaustin.model.user.Rider;
import com.rideaustin.repo.dsl.RideDslRepository;
import com.rideaustin.rest.exception.BadRequestException;
import com.rideaustin.rest.exception.RideAustinException;
import com.rideaustin.service.config.RideJobServiceConfig;
import com.rideaustin.service.config.RidePaymentConfig;
import com.rideaustin.service.event.EventsNotificationService;
import com.rideaustin.service.rating.DriverRatingService;
import com.rideaustin.service.rating.RatingUpdateService;
import com.rideaustin.service.rating.RiderRatingService;
import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;

@RunWith(DataProviderRunner.class)
public class RideRatingServiceTest {

  @Mock
  private FareService fareService;
  @Mock
  private PaymentJobService rideJobService;
  @Mock
  private RatingUpdateService ratingUpdateService;
  @Mock
  private DriverRatingService driverRatingService;
  @Mock
  private RiderRatingService riderRatingService;
  @Mock
  private EventsNotificationService eventsNotificationService;
  @Mock
  private RidePaymentConfig config;
  @Mock
  private RideJobServiceConfig jobsConfig;
  @Mock
  private RideDslRepository rideDslRepository;

  private RideRatingService testedInstance;

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);

    testedInstance = new RideRatingService(fareService, rideJobService, ratingUpdateService, driverRatingService,
      riderRatingService, eventsNotificationService, config, jobsConfig, rideDslRepository);
  }

  @DataProvider
  public static Object[] ineligibleStatuses() {
    return EnumSet.complementOf(EnumSet.of(RideStatus.COMPLETED)).toArray();
  }

  @Test
  @UseDataProvider("ineligibleStatuses")
  public void rateRideAsRiderAbortsOnNotCompletedRides(RideStatus status) throws BadRequestException {
    final long rideId = 1L;
    final Ride ride = new Ride();
    ride.setStatus(status);
    when(rideDslRepository.findOne(eq(rideId))).thenReturn(ride);

    testedInstance.rateRideAsRider(rideId, BigDecimal.ONE, null, null);

    verify(rideDslRepository, never()).save(eq(ride));
  }

  @Test(expected = BadRequestException.class)
  public void rateRideAsRiderThrowsExceptionOnNonEnglishComment() throws BadRequestException {
    final long rideId = 1L;
    final Ride ride = new Ride();
    ride.setStatus(RideStatus.COMPLETED);
    when(rideDslRepository.findOne(eq(rideId))).thenReturn(ride);

    testedInstance.rateRideAsRider(rideId, BigDecimal.ONE, null, "Привет");
  }

  @Test
  public void rateRideAsRiderThrowsExceptionOnAlreadyRatedRide() throws BadRequestException {
    final long rideId = 1L;
    final Ride ride = new Ride();
    ride.setStatus(RideStatus.COMPLETED);
    ride.setDriverRating(5.0);
    when(rideDslRepository.findOne(eq(rideId))).thenReturn(ride);

    expectedException.expectMessage("This ride has already been rated");

    testedInstance.rateRideAsRider(rideId, BigDecimal.ONE, null, null);
  }

  @Test
  public void rateRideAsRiderThrowsExceptionOnAlreadyTippedRide() throws BadRequestException {
    final long rideId = 1L;
    final Ride ride = new Ride();
    ride.setStatus(RideStatus.COMPLETED);
    ride.getFareDetails().setTip(money(10.0));
    when(rideDslRepository.findOne(eq(rideId))).thenReturn(ride);

    expectedException.expectMessage("This ride has already been rated");

    testedInstance.rateRideAsRider(rideId, BigDecimal.ONE, null, null);
  }

  @Test
  public void rateRideAsRiderSetsDriverRating() throws BadRequestException {
    final long rideId = 1L;
    final Ride ride = new Ride();
    final ActiveDriver activeDriver = new ActiveDriver();
    activeDriver.setDriver(new Driver());
    ride.setStatus(RideStatus.COMPLETED);
    ride.setActiveDriver(activeDriver);
    when(rideDslRepository.findOne(eq(rideId))).thenReturn(ride);
    final BigDecimal rating = BigDecimal.ONE;

    testedInstance.rateRideAsRider(rideId, rating, null, null);

    assertEquals(rating.doubleValue(), ride.getDriverRating(), 0.0);
  }

  @Test
  public void rateRideAsRiderThrowsExceptionOnNegativeTip() throws BadRequestException {
    final long rideId = 1L;
    final Ride ride = new Ride();
    final ActiveDriver activeDriver = new ActiveDriver();
    activeDriver.setDriver(new Driver());
    ride.setStatus(RideStatus.COMPLETED);
    ride.setActiveDriver(activeDriver);
    when(rideDslRepository.findOne(eq(rideId))).thenReturn(ride);
    final BigDecimal rating = BigDecimal.ONE;

    expectedException.expectMessage("Tip cannot be less than zero");

    testedInstance.rateRideAsRider(rideId, rating, BigDecimal.valueOf(-1.0), null);
  }

  @Test
  public void rateRideAsRiderSetsTip() throws BadRequestException {
    final long rideId = 1L;
    final Ride ride = new Ride();
    final ActiveDriver activeDriver = new ActiveDriver();
    activeDriver.setDriver(new Driver());
    ride.setStatus(RideStatus.COMPLETED);
    ride.setActiveDriver(activeDriver);
    ride.setCompletedOn(new Date());
    when(rideDslRepository.findOne(eq(rideId))).thenReturn(ride);
    final BigDecimal rating = BigDecimal.ONE;
    final BigDecimal tip = BigDecimal.valueOf(5.0);
    when(jobsConfig.getRidePaymentDelay()).thenReturn(10);
    when(config.getTipLimit()).thenReturn(BigDecimal.valueOf(300));

    testedInstance.rateRideAsRider(rideId, rating, tip, null);

    assertEquals(ride.getFareDetails().getTip().getAmount().doubleValue(), tip.doubleValue(), 0.0);
    assertNotNull(ride.getTippedOn());
  }

  @Test
  public void rateRideAsRiderThrowsExceptionWhenTippingPeriodExpired() throws BadRequestException {
    final long rideId = 1L;
    final Ride ride = new Ride();
    final ActiveDriver activeDriver = new ActiveDriver();
    activeDriver.setDriver(new Driver());
    ride.setStatus(RideStatus.COMPLETED);
    ride.setActiveDriver(activeDriver);
    ride.setCompletedOn(new Date());
    when(rideDslRepository.findOne(eq(rideId))).thenReturn(ride);
    final BigDecimal rating = BigDecimal.ONE;
    final BigDecimal tip = BigDecimal.valueOf(5.0);
    when(jobsConfig.getRidePaymentDelay()).thenReturn(-10);

    expectedException.expectMessage("Tipping period has passed");

    testedInstance.rateRideAsRider(rideId, rating, tip, null);
  }

  @Test
  public void rateRideAsRiderThrowsExceptionOnPaidRide() throws BadRequestException {
    final long rideId = 1L;
    final Ride ride = new Ride();
    final ActiveDriver activeDriver = new ActiveDriver();
    activeDriver.setDriver(new Driver());
    ride.setStatus(RideStatus.COMPLETED);
    ride.setActiveDriver(activeDriver);
    ride.setCompletedOn(new Date());
    ride.setPaymentStatus(PaymentStatus.PAID);
    when(rideDslRepository.findOne(eq(rideId))).thenReturn(ride);
    final BigDecimal rating = BigDecimal.ONE;
    final BigDecimal tip = BigDecimal.valueOf(5.0);
    when(jobsConfig.getRidePaymentDelay()).thenReturn(10);

    expectedException.expectMessage("Ride has already been charged");

    testedInstance.rateRideAsRider(rideId, rating, tip, null);
  }

  @Test
  public void rateRideAsRiderThrowsExceptionOnExcessiveTip() throws BadRequestException {
    final long rideId = 1L;
    final Ride ride = new Ride();
    final ActiveDriver activeDriver = new ActiveDriver();
    activeDriver.setDriver(new Driver());
    ride.setStatus(RideStatus.COMPLETED);
    ride.setActiveDriver(activeDriver);
    ride.setCompletedOn(new Date());
    when(rideDslRepository.findOne(eq(rideId))).thenReturn(ride);
    final BigDecimal rating = BigDecimal.ONE;
    final BigDecimal tipLimit = BigDecimal.TEN;
    when(jobsConfig.getRidePaymentDelay()).thenReturn(10);
    when(config.getTipLimit()).thenReturn(tipLimit);
    final BigDecimal tip = tipLimit.add(BigDecimal.ONE);

    expectedException.expectMessage(startsWith("Tip cannot be more than "));

    testedInstance.rateRideAsRider(rideId, rating, tip, null);
  }

  @Test
  @UseDataProvider("ineligibleStatuses")
  public void rateRideAsDriverAbortsOnNotCompletedRides(RideStatus status) throws RideAustinException {
    final long rideId = 1L;
    final Ride ride = new Ride();
    ride.setStatus(status);
    when(rideDslRepository.findOne(eq(rideId))).thenReturn(ride);

    testedInstance.rateRideAsDriver(rideId, BigDecimal.ONE, null);

    verify(rideDslRepository, never()).save(eq(ride));
  }

  @Test(expected = BadRequestException.class)
  public void rateRideAsDriverThrowsExceptionOnNonEnglishComment() throws RideAustinException {
    final long rideId = 1L;
    final Ride ride = new Ride();
    ride.setStatus(RideStatus.COMPLETED);
    when(rideDslRepository.findOne(eq(rideId))).thenReturn(ride);

    testedInstance.rateRideAsDriver(rideId, BigDecimal.ONE, "Привет");
  }

  @Test
  public void rateRideAsDriverThrowsExceptionOnAlreadyRatedRide() throws RideAustinException {
    final long rideId = 1L;
    final Ride ride = new Ride();
    ride.setStatus(RideStatus.COMPLETED);
    ride.setRiderRating(5.0);
    ride.setRider(new Rider());
    when(rideDslRepository.findOne(eq(rideId))).thenReturn(ride);

    expectedException.expectMessage("This ride has already been rated");

    testedInstance.rateRideAsDriver(rideId, BigDecimal.ONE,null);
  }

  @Test
  public void rateRideAsDriverSetsRiderRating() throws RideAustinException {
    final long rideId = 1L;
    final Ride ride = new Ride();
    final ActiveDriver activeDriver = new ActiveDriver();
    activeDriver.setDriver(new Driver());
    ride.setStatus(RideStatus.COMPLETED);
    ride.setActiveDriver(activeDriver);
    when(rideDslRepository.findOne(eq(rideId))).thenReturn(ride);
    final BigDecimal rating = BigDecimal.ONE;

    testedInstance.rateRideAsDriver(rideId, rating, null);

    assertEquals(rating.doubleValue(), ride.getRiderRating(), 0.0);
  }

}