package com.rideaustin.dispatch;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.rideaustin.test.util.TestUtils;
import com.rideaustin.testrail.TestCases;

@Category(Dispatch.class)
public class C1177112SUVDriverSUVCarIT extends BaseC1177112Test {

  @Override
  @Before
  public void setUp() throws Exception {
    super.setUp();
    this.driver = setup.getSuvCarDriver();
  }

  @Test
  @TestCases("C1177112")
  public void testSUVDriverWithSUVCarShouldReceiveSUVRequest() throws Exception {
    driverAction
      .locationUpdate(driver, AUSTIN_CENTER.lat, AUSTIN_CENTER.lng, new String[]{TestUtils.SUV})
      .andExpect(status().isOk());

    Long ride = riderAction.requestRide(rider.getEmail(), AUSTIN_CENTER, TestUtils.SUV);

    awaitDispatch(driver, ride);

  }

}
