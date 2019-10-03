package com.rideaustin.rideupgrade;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.rideaustin.Constants;
import com.rideaustin.model.enums.EventType;
import com.rideaustin.model.enums.RideUpgradeRequestStatus;
import com.rideaustin.model.user.Driver;
import com.rideaustin.model.user.Rider;
import com.rideaustin.testrail.TestCases;

@Category(RideUpgrade.class)
public class C1226706IT extends AbstractRideUpgradeTest {

  @Test
  @TestCases("C1226706")
  public void test() throws Exception {
    Driver driver = ride.getActiveDriver().getDriver();
    Rider rider = ride.getRider();
    String source = ride.getRequestedCarType().getCarCategory();

    issueRequest(driver, rider, source, ride.getId());

    acceptRequest(driver, rider, source, ride.getId());

    driverAction
      .cancelRideUpgrade(driver.getEmail())
      .andExpect(status().isNotFound())
      .andExpect(content().string("\"Upgrade request is not found or already expired\""));

    eventAssertHelper.assertLastEventIsSent(driver, EventType.RIDE_UPGRADE_ACCEPTED);
    assertRequest(driver, rider, source, RideUpgradeRequestStatus.ACCEPTED, Constants.NEUTRAL_SURGE_FACTOR, ride.getId());
  }
}
