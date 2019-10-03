package com.rideaustin.dispatch.womenonly;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.LinkedList;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.rideaustin.test.actions.cascaded.CascadedDriverAction;
import com.rideaustin.test.asserts.DispatchHistoryAssert;
import com.rideaustin.test.setup.DefaultWODispatchSetup;
import com.rideaustin.testrail.TestCases;

@Category(WomenOnly.class)
public class C1177079IT extends AbstractWomenOnlyDispatchTest<DefaultWODispatchSetup> {

  @Override
  @Before
  public void setUp() throws Exception {
    super.setUp();
  }

  @Test
  @TestCases("C1177079")
  public void test() throws Exception {
    driverAction.goOnline(woDriver.getDriver().getEmail(), AUSTIN_CENTER, new String[]{CAR_TYPE})
      .andExpect(status().isOk());
    driverAction.locationUpdate(woDriver, AUSTIN_CENTER.lat, AUSTIN_CENTER.lng, new String[]{CAR_TYPE}, new String[0])
      .andExpect(status().isOk());

    Long ride = riderAction.requestRide(rider.getEmail(), AUSTIN_CENTER, CAR_TYPE);

    awaitDispatch(woDriver, ride);

    CascadedDriverAction cascadedDriverAction = new CascadedDriverAction(woDriver.getDriver(), driverAction);

    cascadedDriverAction
      .withRideId(ride)
      .acceptRide().reach().cancelRide();

    LinkedList<Long> dispatchHistory = getDispatchHistory(ride);
    DispatchHistoryAssert.assertThat(dispatchHistory)
      .hasLength(1)
      .isDispatchedFirstTo(woDriver.getId());
  }
}
