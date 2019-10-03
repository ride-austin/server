package com.rideaustin.applepay;

import static org.junit.Assert.assertNotNull;

import javax.inject.Inject;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.google.maps.model.LatLng;
import com.rideaustin.Constants;
import com.rideaustin.filter.ClientType;
import com.rideaustin.model.enums.RideStatus;
import com.rideaustin.model.ride.ActiveDriver;
import com.rideaustin.repo.dsl.RideDslRepository;
import com.rideaustin.rest.model.MobileRiderRideDto;
import com.rideaustin.test.asserts.RiderRideAssert;
import com.rideaustin.test.setup.C1181743Setup;
import com.rideaustin.test.util.TestUtils;
import com.rideaustin.testrail.TestCases;

@Category(ApplePay.class)
public class C1181743IT extends AbstractApplePayTest<C1181743Setup> {

  private LatLng defaultClosestLocation = new LatLng(30.269372, -97.740394);
  private LatLng defaultNotClosestLocation = new LatLng(30.263372, -97.744394);

  @Inject
  private RideDslRepository repository;
  private ActiveDriver firstDriver;
  private ActiveDriver secondDriver;

  @Override
  @Before
  public void setUp() throws Exception {
    super.setUp();
    firstDriver = setup.getFirstDriver();
    secondDriver = setup.getSecondDriver();
    configurationItemCache.setBooleanConfig(Constants.DEFAULT_CITY_ID, ClientType.CONSOLE, "redispatchOnCancel", "enabled", true);
  }

  @Test
  @TestCases("C1181743")
  public void testResubmitToAvailableDriver() throws Exception {

    assertNumberOfActiveDriver(2);

    driverAction.locationUpdate(firstDriver, defaultClosestLocation.lat, defaultClosestLocation.lng);
    driverAction.locationUpdate(secondDriver, defaultNotClosestLocation.lat, defaultNotClosestLocation.lng);

    Long rideId = riderAction.requestRide(rider.getEmail(), defaultClosestLocation, TestUtils.REGULAR, null, SAMPLE_TOKEN);

    awaitDispatch(firstDriver, rideId);

    driverAction.acceptRide(firstDriver.getDriver().getEmail(), rideId);
    sleeper.sleep(5000);
    driverAction.locationUpdate(firstDriver, defaultClosestLocation.lat, defaultClosestLocation.lng);
    driverAction.cancelRide(firstDriver.getDriver().getEmail(), rideId);

    awaitDispatch(secondDriver, rideId);

    driverAction.acceptRide(secondDriver.getDriver().getEmail(), rideId);
    MobileRiderRideDto rideInfo = riderAction.getRideInfo(rider.getEmail(), rideId);
    RiderRideAssert.assertThat(rideInfo)
      .hasStatus(RideStatus.DRIVER_ASSIGNED)
      .hasDriverAssigned(secondDriver.getId());

    assertNotNull(repository.findOne(rideId).getPreChargeId());

  }
}
