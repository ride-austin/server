package com.rideaustin.applepay;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.google.maps.model.LatLng;
import com.rideaustin.model.ride.ActiveDriver;
import com.rideaustin.test.setup.DefaultApplePaySetup;
import com.rideaustin.testrail.TestCases;

@Category(ApplePay.class)
public class C1181786IT extends AbstractApplePayTest<DefaultApplePaySetup> {

  @Override
  @Before
  public void setUp() throws Exception {
    super.setUp();
    stripeServiceMock.setFailOnApplePayCharge(true);
    stripeServiceMock.setFailOnCardCharge(true);
  }

  @Test
  @TestCases("C1181786")
  public void test() throws Exception {
    LatLng pickupLocation = locationProvider.getCenter();
    LatLng dropoffLocation = locationProvider.getOutsideAirportLocation();
    ActiveDriver driver = setup.getActiveDriver();

    Long ride = performRide(pickupLocation, dropoffLocation, driver, SAMPLE_TOKEN);

    paymentService.processRidePayment(ride);

    riderAction.setPrimaryCard(rider)
      .andExpect(status().is4xxClientError());
  }
}
