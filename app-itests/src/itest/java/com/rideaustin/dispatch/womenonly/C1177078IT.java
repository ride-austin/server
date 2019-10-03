package com.rideaustin.dispatch.womenonly;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.LinkedList;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.rideaustin.model.enums.RideStatus;
import com.rideaustin.model.ride.DriverType;
import com.rideaustin.test.asserts.DispatchHistoryAssert;
import com.rideaustin.test.setup.DefaultWODispatchSetup;
import com.rideaustin.testrail.TestCases;

@Category(WomenOnly.class)
public class C1177078IT extends AbstractWomenOnlyDispatchTest<DefaultWODispatchSetup> {

  @Override
  @Before
  public void setUp() throws Exception {
    super.setUp();
  }

  @Test
  @TestCases("C1177078")
  public void test() throws Exception {
    driverAction.locationUpdate(woDriver, AUSTIN_CENTER.lat, AUSTIN_CENTER.lng, new String[]{CAR_TYPE}, new String[]{DriverType.WOMEN_ONLY})
      .andExpect(status().isOk());

    Long ride = riderAction.requestRide(rider.getEmail(), AUSTIN_CENTER, CAR_TYPE);

    awaitStatus(ride, RideStatus.NO_AVAILABLE_DRIVER);

    LinkedList<Long> dispatchHistory = getDispatchHistory(ride);
    DispatchHistoryAssert.assertThat(dispatchHistory)
      .isEmpty();
  }
}
