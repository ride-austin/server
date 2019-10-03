package com.rideaustin.applepay;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.google.maps.model.LatLng;
import com.rideaustin.Constants;
import com.rideaustin.model.ride.ActiveDriver;
import com.rideaustin.test.setup.DefaultApplePaySetup;
import com.rideaustin.testrail.TestCases;

@Category(ApplePay.class)
public class C1181755IT extends AbstractApplePayTest<DefaultApplePaySetup> {

  @Test
  @TestCases("C1181755")
  public void test() throws Exception {
    LatLng pickupLocation = locationProvider.getCenter();
    LatLng dropoffLocation = locationProvider.getOutsideAirportLocation();
    ActiveDriver driver = setup.getActiveDriver();

    Long ride = performRide(pickupLocation, dropoffLocation, driver, SAMPLE_TOKEN);

    paymentService.processRidePayment(ride);

    assertThat(stripeServiceMock.getApplePayCharged(), is(not(Constants.ZERO_USD)));
    assertThat(stripeServiceMock.getCardCharged(), is(Constants.ZERO_USD));

    stripeServiceMock.resetFlags();

    ride = performRide(pickupLocation, dropoffLocation, driver, null);

    paymentService.processRidePayment(ride);

    assertThat(stripeServiceMock.getApplePayCharged(), is(Constants.ZERO_USD));
    assertThat(stripeServiceMock.getCardCharged(), is(not(Constants.ZERO_USD)));
  }

}
