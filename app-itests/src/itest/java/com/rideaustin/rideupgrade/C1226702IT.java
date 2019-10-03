package com.rideaustin.rideupgrade;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.rideaustin.model.enums.RideUpgradeRequestStatus;
import com.rideaustin.model.user.Driver;
import com.rideaustin.testrail.TestCases;

@Category(RideUpgrade.class)
public class C1226702IT extends AbstractRideUpgradeTest {

  @Test
  @TestCases("C1226702")
  public void test() throws Exception {
    Driver driver = ride.getActiveDriver().getDriver();
    String driverEmail = driver.getEmail();

    issueRequest(driver, ride.getRider(), ride.getRequestedCarType().getCarCategory(), ride.getId());

    assertRideCategoryAndStatus(driverEmail, ride.getRequestedCarType().getCarCategory(), RideUpgradeRequestStatus.REQUESTED);
  }
}
