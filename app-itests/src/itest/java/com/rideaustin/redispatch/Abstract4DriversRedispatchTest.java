package com.rideaustin.redispatch;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Before;

import com.google.maps.model.LatLng;
import com.rideaustin.model.ride.ActiveDriver;
import com.rideaustin.test.setup.Default4DriversRedispatchTestSetup;

public abstract class Abstract4DriversRedispatchTest extends AbstractRedispatchTest<Default4DriversRedispatchTestSetup> {

  protected ActiveDriver thirdDriver;
  protected ActiveDriver fourthDriver;
  protected LatLng thirdDriverLocation;
  protected LatLng fourthDriverLocation;

  @Override
  @Before
  public void setUp() throws Exception {
    super.setUp();
    thirdDriver = this.setup.getThirdActiveDriver();
    fourthDriver = this.setup.getFourthActiveDriver();
  }

  @Override
  protected void goOnline(ActiveDriver firstDriver, ActiveDriver secondDriver, LatLng firstDriverLocation, LatLng secondDriverLocation) throws Exception {
    super.goOnline(firstDriver, secondDriver, firstDriverLocation, secondDriverLocation);
    thirdDriverLocation = new LatLng(firstDriverLocation.lat + 0.02, firstDriverLocation.lng + 0.02);
    fourthDriverLocation = new LatLng(firstDriverLocation.lat + 0.03, firstDriverLocation.lng + 0.03);

    driverAction.locationUpdate(thirdDriver, thirdDriverLocation.lat, thirdDriverLocation.lng)
      .andExpect(status().isOk());
    driverAction.locationUpdate(fourthDriver, fourthDriverLocation.lat, fourthDriverLocation.lng)
      .andExpect(status().isOk());
  }

  @Override
  protected void assertRideRedispatched(LatLng destination, ActiveDriver secondDriver, LatLng riderLocation, LatLng secondDriverLocation, Long ride) throws Exception {
    super.assertRideRedispatched(destination, fourthDriver, riderLocation, fourthDriverLocation, ride);
  }
}
