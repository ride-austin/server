package com.rideaustin.dispatch;

import javax.inject.Inject;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.maps.model.LatLng;
import com.rideaustin.Constants;
import com.rideaustin.filter.ClientType;
import com.rideaustin.model.enums.RideStatus;
import com.rideaustin.model.ride.ActiveDriver;
import com.rideaustin.model.user.Rider;
import com.rideaustin.service.thirdparty.StripeServiceMockImpl;
import com.rideaustin.test.setup.DefaultRedispatchTestSetup;
import com.rideaustin.test.util.TestUtils;

public class RA15112IT extends BaseDispatchTest<DefaultRedispatchTestSetup> {

  private LatLng defaultClosestLocation = new LatLng(30.269372, -97.740394);

  private Rider rider;
  private ActiveDriver driver;

  @Inject
  private StripeServiceMockImpl stripeServiceMock;

  @Before
  public void setUp() throws Exception {
    super.setUp();
    rider = this.setup.getRider();
    driver = this.setup.getFirstActiveDriver();
    configurationItemCache.setBooleanConfig(Constants.DEFAULT_CITY_ID, ClientType.CONSOLE, "ridePayment", "asyncPreauthEnabled", true);
  }

  @Test
  public void test() throws Exception {
    driverAction.locationUpdate(driver, defaultClosestLocation.lat, defaultClosestLocation.lng);

    stripeServiceMock.setFailOnCardCharge(true);

    Long rideId = riderAction.requestRide(rider.getEmail(), defaultClosestLocation, TestUtils.REGULAR);

    awaitDispatch(driver, rideId);

    awaitStatus(rideId, RideStatus.NO_AVAILABLE_DRIVER);
  }

  @After
  public void tearDown() throws Exception {
    super.supportTearDown();
    stripeServiceMock.setFailOnCardCharge(false);
  }
}
