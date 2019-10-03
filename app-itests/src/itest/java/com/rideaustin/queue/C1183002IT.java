package com.rideaustin.queue;

import static com.rideaustin.model.enums.EventType.QUEUED_AREA_LEAVING_RIDE;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import javax.inject.Inject;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.google.maps.model.LatLng;
import com.rideaustin.model.ride.ActiveDriver;
import com.rideaustin.model.user.Driver;
import com.rideaustin.testrail.TestCases;
import com.rideaustin.test.asserts.EventAssertHelper;

@Category(AirportQueue.class)
public class C1183002IT extends AbstractDispatchedRideInQueueTest {

  @Inject
  private EventAssertHelper eventAssertHelper;

  @Override
  @Before
  public void setUp() throws Exception {
    super.setUp();
  }

  @Test
  @TestCases("C1183002")
  public void test() throws Exception {

    ActiveDriver activeDriver = setup.getFirstDriver();
    Driver driver = activeDriver.getDriver();

    //when
    LatLng airportLocation = locationProvider.getAirportLocation();
    driverAction
      .locationUpdate(activeDriver, airportLocation.lat, airportLocation.lng)
      .andExpect(status().isOk());
    updateQueue();
    Long ride = riderAction.requestRide(setup.getRider().getEmail(), airportLocation);

    //given
    acceptAndStartRide(activeDriver, ride);

    //then
    eventAssertHelper.assertLastEventIsSent(driver, QUEUED_AREA_LEAVING_RIDE);
  }

}
