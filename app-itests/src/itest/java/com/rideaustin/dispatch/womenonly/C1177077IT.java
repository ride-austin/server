package com.rideaustin.dispatch.womenonly;

import com.rideaustin.model.enums.RideStatus;
import com.rideaustin.model.ride.DriverType;
import com.rideaustin.test.asserts.DispatchHistoryAssert;
import com.rideaustin.test.setup.DefaultWODispatchSetup;
import com.rideaustin.testrail.TestCases;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.LinkedList;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Category(WomenOnly.class)
public class C1177077IT extends AbstractWomenOnlyDispatchTest<DefaultWODispatchSetup> {

  @Test
  @TestCases("C1177077")
  public void test() throws Exception {
    driverAction.locationUpdate(woDriver, AUSTIN_CENTER.lat, AUSTIN_CENTER.lng, new String[]{CAR_TYPE}, new String[]{DriverType.WOMEN_ONLY})
      .andExpect(status().isOk());
    driverAction.locationUpdate(regularDriver, AUSTIN_CENTER.lat, AUSTIN_CENTER.lng, new String[]{CAR_TYPE})
      .andExpect(status().isOk());

    Long ride = riderAction.requestRide(rider.getEmail(), AUSTIN_CENTER, CAR_TYPE, (String) DriverType.WOMEN_ONLY);

    awaitDispatch(woDriver, ride);
    driverAction.acceptRide(woDriver, ride)
      .andExpect(status().isOk());
    awaitStatus(ride, RideStatus.DRIVER_ASSIGNED);

    LinkedList<Long> dispatchHistory = getDispatchHistory(ride);
    DispatchHistoryAssert.assertThat(dispatchHistory)
      .hasLength(1)
      .isDispatchedFirstTo(woDriver.getId());
  }

}
