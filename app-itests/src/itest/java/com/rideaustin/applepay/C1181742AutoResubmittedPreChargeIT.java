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
import com.rideaustin.redispatch.Redispatch;
import com.rideaustin.repo.dsl.RideDslRepository;
import com.rideaustin.rest.model.MobileRiderRideDto;
import com.rideaustin.test.asserts.RiderRideAssert;
import com.rideaustin.test.setup.DefaultApplePaySetup;
import com.rideaustin.test.stubs.ConfigurationItemCache;
import com.rideaustin.test.util.TestUtils;
import com.rideaustin.testrail.TestCases;

@Category({ApplePay.class, Redispatch.class})
public class C1181742AutoResubmittedPreChargeIT extends AbstractApplePayTest<DefaultApplePaySetup> {

  private LatLng defaultClosestLocation = new LatLng(30.269372, -97.740394);

  @Inject
  private RideDslRepository repository;

  private ActiveDriver activeDriver;

  @Inject
  private ConfigurationItemCache configurationItemCache;

  @Override
  @Before
  public void setUp() throws Exception {
    super.setUp();
    activeDriver = setup.getActiveDriver();
    configurationItemCache.setBooleanConfig(Constants.DEFAULT_CITY_ID, ClientType.CONSOLE, "redispatchOnCancel", "enabled", false);
  }

  @Test
  @TestCases("C1181742")
  public void test() throws Exception {

    assertNumberOfActiveDriver(1);

    driverAction.locationUpdate(activeDriver, defaultClosestLocation.lat, defaultClosestLocation.lng);

    Long rideId = riderAction.requestRide(rider.getEmail(), PICKUP_LOCATION, TestUtils.REGULAR, null, SAMPLE_TOKEN);

    awaitDispatch(activeDriver, rideId);

    driverAction.acceptRide(activeDriver.getDriver().getEmail(), rideId);

    sleeper.sleep(5000);

    driverAction.locationUpdate(activeDriver, defaultClosestLocation.lat, defaultClosestLocation.lng);

    driverAction.cancelRide(activeDriver.getDriver().getEmail(), rideId);

    sleeper.sleep(7000);

    MobileRiderRideDto rideInfo = riderAction.getRideInfo(rider.getEmail(), rideId);
    RiderRideAssert.assertThat(rideInfo)
      .hasStatus(RideStatus.DRIVER_CANCELLED);

    assertNotNull(repository.findOne(rideId).getPreChargeId());

  }

}
