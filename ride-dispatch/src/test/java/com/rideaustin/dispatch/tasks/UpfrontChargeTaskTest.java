package com.rideaustin.dispatch.tasks;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.rideaustin.model.enums.PaymentStatus;
import com.rideaustin.model.enums.RideStatus;
import com.rideaustin.model.enums.SplitFareStatus;
import com.rideaustin.model.ride.Ride;
import com.rideaustin.repo.dsl.FarePaymentDslRepository;
import com.rideaustin.repo.dsl.RideDslRepository;
import com.rideaustin.rest.exception.RideAustinException;
import com.rideaustin.rest.model.SplitFareDto;
import com.rideaustin.service.payment.UpfrontPaymentService;

public class UpfrontChargeTaskTest {

  @Mock
  private UpfrontPaymentService paymentService;
  @Mock
  private RideDslRepository rideDslRepository;
  @Mock
  private FarePaymentDslRepository farePaymentDslRepository;

  private UpfrontChargeTask testedInstance;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);

    testedInstance = new UpfrontChargeTask(paymentService, rideDslRepository, farePaymentDslRepository);
  }

  @Test
  public void runSkipsTaskWithoutRideId() throws RideAustinException {
    testedInstance.run();

    verify(paymentService, never()).processRidePayment(any(Ride.class));
  }

  @Test
  public void runSkipsTaskForRideWithoutDestination() throws RideAustinException {
    final long rideId = 1L;
    when(rideDslRepository.findOne(eq(rideId))).thenReturn(new Ride());

    testedInstance.withRideId(rideId).run();

    verify(paymentService, never()).processRidePayment(any(Ride.class));
  }

  @Test
  public void runSkipsPaidRides() throws RideAustinException {
    final long rideId = 1L;
    final Ride ride = new Ride();
    ride.setId(rideId);
    ride.setEndLocationLat(34.65161);
    ride.setEndLocationLong(-97.46816);
    ride.setPaymentStatus(PaymentStatus.PAID);
    when(rideDslRepository.findOne(eq(rideId))).thenReturn(ride);

    testedInstance.withRideId(rideId).run();

    verify(paymentService, never()).processRidePayment(any(Ride.class));
  }

  @Test
  public void runSkipsSplitPaymentRide() throws RideAustinException {
    final long rideId = 1L;
    final Ride ride = new Ride();
    ride.setId(rideId);
    ride.setEndLocationLat(34.65161);
    ride.setEndLocationLong(-97.46816);
    ride.setPaymentStatus(null);
    when(rideDslRepository.findOne(eq(rideId))).thenReturn(ride);
    when(farePaymentDslRepository.findFarePayments(eq(rideId))).thenReturn(Collections.singletonList(new SplitFareDto(1L,
      1L, 1L, "A", "B", SplitFareStatus.ACCEPTED, new Date(), new Date(), "C", "D")));

    testedInstance.withRideId(rideId).run();

    verify(paymentService, never()).processRidePayment(any(Ride.class));
  }

  @Test
  public void runSkipsCompletedRides() throws RideAustinException {
    final long rideId = 1L;
    final Ride ride = new Ride();
    ride.setId(rideId);
    ride.setEndLocationLat(34.65161);
    ride.setEndLocationLong(-97.46816);
    ride.setPaymentStatus(null);
    ride.setStatus(RideStatus.COMPLETED);
    when(rideDslRepository.findOne(eq(rideId))).thenReturn(ride);

    testedInstance.withRideId(rideId).run();

    verify(paymentService, never()).processRidePayment(any(Ride.class));
  }

  @Test
  public void runPerformsPayment() throws RideAustinException {
    final long rideId = 1L;
    final Ride ride = new Ride();
    ride.setId(rideId);
    ride.setEndLocationLat(34.65161);
    ride.setEndLocationLong(-97.46816);
    ride.setPaymentStatus(null);
    ride.setStatus(RideStatus.ACTIVE);
    when(rideDslRepository.findOne(eq(rideId))).thenReturn(ride);

    testedInstance.withRideId(rideId).run();

    verify(paymentService, times(1)).processRidePayment(eq(ride));
  }

}