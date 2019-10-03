package com.rideaustin.dispatch.womenonly;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Before;
import org.junit.Test;

import com.rideaustin.filter.ClientType;
import com.rideaustin.model.ride.ActiveDriver;
import com.rideaustin.test.actions.cascaded.CascadedDriverAction;
import com.rideaustin.test.setup.DefaultWODispatchSetup;
import com.rideaustin.testrail.TestCases;

public class MultipleDriverTypesIT extends AbstractWomenOnlyDispatchTest<DefaultWODispatchSetup> {

  private ActiveDriver driver;

  @Override
  @Before
  public void setUp() throws Exception {
    super.setUp();
    driver = setup.getWoFPDriver();
    configurationItemCache.setBooleanConfig(1L, ClientType.CONSOLE, "redispatchOnCancel", "enabled", false);
  }

  @Test
  @TestCases("C1177077")
  public void test() throws Exception {
    driverAction.locationUpdate(driver, AUSTIN_CENTER.lat, AUSTIN_CENTER.lng, new String[]{CAR_TYPE}, new String[]{"WOMEN_ONLY","FINGERPRINTED"})
      .andExpect(status().isOk());

    Long ride = riderAction.requestRide(rider.getEmail(), AUSTIN_CENTER, CAR_TYPE, "WOMEN_ONLY,FINGERPRINTED");

    awaitDispatch(driver, ride);

    CascadedDriverAction cascadedDriverAction = new CascadedDriverAction(driver.getDriver(), driverAction)
      .withRideId(ride);

    cascadedDriverAction.acceptRide().cancelRide();
  }
}
