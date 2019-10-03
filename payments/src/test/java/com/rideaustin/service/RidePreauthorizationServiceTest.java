package com.rideaustin.service;

import static org.junit.Assert.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.rideaustin.model.ride.CarType;
import com.rideaustin.model.ride.Ride;
import com.rideaustin.model.user.Rider;
import com.rideaustin.rest.exception.RideAustinException;
import com.rideaustin.service.thirdparty.StripeService;

public class RidePreauthorizationServiceTest {

  @Mock
  private StripeService stripeService;
  @Mock
  private CarType.Configuration configuration;

  private RidePreauthorizationService testedInstance;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);

    testedInstance = new RidePreauthorizationService(stripeService);
  }

  @Test
  public void preauthorizeRideSkipsWhenConfiguredToSkip() throws RideAustinException {
    when(configuration.isSkipRideAuthorization()).thenReturn(true);
    final Ride ride = new Ride();
    ride.setRequestedCarType(new CarType());

    final String result = testedInstance.preauthorizeRide(ride, configuration, null, () -> {
    });

    assertNull(result);
  }

  @Test
  public void preauthorizeRideChargesCard() throws RideAustinException {
    when(configuration.isSkipRideAuthorization()).thenReturn(false);
    final Ride ride = new Ride();
    ride.setRequestedCarType(new CarType());
    ride.setRider(new Rider());

    final String result = testedInstance.preauthorizeRide(ride, configuration, null, () -> {
    });

    verify(stripeService).authorizeRide(ride, ride.getRider().getPrimaryCard());
  }

  @Test
  public void preauthorizeRideChargesApplePay() throws RideAustinException {
    when(configuration.isSkipRideAuthorization()).thenReturn(false);
    final Ride ride = new Ride();
    ride.setRequestedCarType(new CarType());
    ride.setRider(new Rider());
    final String applePayToken = "token";

    final String result = testedInstance.preauthorizeRide(ride, configuration, applePayToken, () -> {
    });

    verify(stripeService).authorizeRide(ride, applePayToken);
  }
}