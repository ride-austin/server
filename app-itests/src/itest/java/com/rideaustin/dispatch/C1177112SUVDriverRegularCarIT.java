package com.rideaustin.dispatch;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.LinkedList;

import org.junit.Before;
import org.junit.Test;

import com.rideaustin.model.enums.RideStatus;
import com.rideaustin.test.asserts.DispatchHistoryAssert;
import com.rideaustin.test.util.TestUtils;
import com.rideaustin.testrail.TestCases;

public class C1177112SUVDriverRegularCarIT extends BaseC1177112Test {

  @Override
  @Before
  public void setUp() throws Exception {
    super.setUp();
    this.driver = setup.getRegularCarDriver();
  }

  @Test
  @TestCases("C1177112")
  public void testSUVDriverWithRegularCarShouldNotReceiveSUVRequest() throws Exception {
    driverAction
      .locationUpdate(driver, AUSTIN_CENTER.lat, AUSTIN_CENTER.lng, new String[]{TestUtils.SUV})
      .andExpect(status().isBadRequest());

    Long ride = riderAction.requestRide(rider.getEmail(), AUSTIN_CENTER, TestUtils.SUV);

    awaitStatus(ride, RideStatus.NO_AVAILABLE_DRIVER);

    LinkedList<Long> dispatchHistory = getDispatchHistory(ride);

    DispatchHistoryAssert.assertThat(dispatchHistory)
      .isEmpty();
  }
}
