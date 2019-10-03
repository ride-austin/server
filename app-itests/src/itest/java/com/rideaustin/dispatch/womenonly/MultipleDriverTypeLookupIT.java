package com.rideaustin.dispatch.womenonly;

import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.google.maps.model.LatLng;
import com.rideaustin.model.ride.DriverType;
import com.rideaustin.rest.model.CompactActiveDriverDto;
import com.rideaustin.test.setup.MultipleDriverTypeLookupSetup;
import com.rideaustin.test.util.TestUtils;

public class MultipleDriverTypeLookupIT extends AbstractWomenOnlyDispatchTest<MultipleDriverTypeLookupSetup> {

  @Override
  @Before
  public void setUp() throws Exception {
    super.setUp();
    setup = createSetup();
  }

  @Test
  public void test() throws Exception {
    final LatLng location = locationProvider.getCenter();
    driverAction.goOnline(regularDriver.getDriver().getEmail(), location);
    driverAction.locationUpdate(regularDriver, location.lat, location.lng);

    final List<CompactActiveDriverDto> result = riderAction.searchDrivers(rider.getEmail(), location, TestUtils.REGULAR,
      String.format("%s,%s", DriverType.WOMEN_ONLY, DriverType.FINGERPRINTED));

    assertTrue(result.isEmpty());
  }
}
