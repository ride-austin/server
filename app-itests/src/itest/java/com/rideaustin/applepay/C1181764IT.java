package com.rideaustin.applepay;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

import javax.inject.Inject;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.google.maps.model.LatLng;
import com.rideaustin.Constants;
import com.rideaustin.model.ride.ActiveDriver;
import com.rideaustin.model.ride.Ride;
import com.rideaustin.repo.dsl.RideDslRepository;
import com.rideaustin.test.setup.DefaultApplePaySetup;
import com.rideaustin.testrail.TestCases;
import com.rideaustin.utils.SafeZeroUtils;

@Category(ApplePay.class)
public class C1181764IT extends AbstractApplePayTest<DefaultApplePaySetup> {

  @Inject
  private RideDslRepository rideDslRepository;

  @Test
  @TestCases("C1181764")
  public void test() throws Exception {
    LatLng pickupLocation = locationProvider.getCenter();
    LatLng dropoffLocation = locationProvider.getOutsideAirportLocation();
    ActiveDriver driver = setup.getActiveDriver();

    Long ride = performRide(pickupLocation, dropoffLocation, driver, SAMPLE_TOKEN);

    stripeServiceMock.setFailOnApplePayCharge(true);

    paymentService.processRidePayment(ride);

    assertThat(stripeServiceMock.getCardCharged(), is(not(Constants.ZERO_USD)));
    assertThat(stripeServiceMock.getApplePayCharged(), is(Constants.ZERO_USD));

    Ride storedRide = rideDslRepository.findOne(ride);
    assertNull(storedRide.getCharity());
    assertEquals(Constants.ZERO_USD, SafeZeroUtils.safeZero(storedRide.getRoundUpAmount()));
  }
}
