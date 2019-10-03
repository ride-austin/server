package com.rideaustin.redispatch;

import static org.junit.Assert.fail;

import java.util.Optional;

import javax.inject.Inject;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.google.maps.model.LatLng;
import com.rideaustin.model.enums.RideStatus;
import com.rideaustin.model.ride.ActiveDriver;
import com.rideaustin.model.ride.CityCarType;
import com.rideaustin.model.ride.Ride;
import com.rideaustin.rest.model.MobileRiderRideDto;
import com.rideaustin.service.CarTypeService;
import com.rideaustin.service.RideService;
import com.rideaustin.service.config.RidePaymentConfig;
import com.rideaustin.test.asserts.RiderRideAssert;
import com.rideaustin.test.util.TestUtils;
import com.rideaustin.testrail.TestCases;

/**
 * https://testrail.devfactory.com/index.php?/cases/view/1182983
 */
@Category(Redispatch.class)
public class C1182984IT extends AbstractRiderCancelRedispatchTest {

  @Inject
  private RidePaymentConfig config;

  @Inject
  private RideService rideService;

  @Inject
  private CarTypeService carTypeService;

  @Test
  @TestCases("C1182984")
  public void test() throws Exception {
    doTestRedispatch(null);
  }

  @Override
  protected void waitForCancellation() {
    sleeper.sleep((config.getCancellationChargeFreePeriod()+1)*1000);
  }

  @Override
  protected void assertRideRedispatched(LatLng destination, ActiveDriver secondDriver, LatLng riderLocation, LatLng secondDriverLocation, Long ride) throws Exception {
    MobileRiderRideDto rideInfo = riderAction.getRideInfo(rider.getEmail(), ride);

    Ride savedRide = rideService.getRide(ride);
    Optional<CityCarType> cityCarType = carTypeService.getCityCarType(savedRide.getRequestedCarType(), savedRide.getCityId());
    if (!cityCarType.isPresent()) {
      fail("Car type is expected to be present");
    }

    RiderRideAssert.assertThat(rideInfo)
      .hasRequestedCategory(TestUtils.REGULAR)
      .hasStatus(RideStatus.RIDER_CANCELLED)
      .hasStartLocation(riderLocation)
      .hasEndLocation(destination);
  }
}
