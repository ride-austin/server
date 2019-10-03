package com.rideaustin.rideupgrade;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.rideaustin.model.enums.RideUpgradeRequestStatus;
import com.rideaustin.model.user.Driver;
import com.rideaustin.model.user.Rider;
import com.rideaustin.testrail.TestCases;

@Category(RideUpgrade.class)
public class C1226704IT extends AbstractRideUpgradeTest {

  @Test
  @TestCases("C1226704")
  public void test() throws Exception {
    Driver driver = ride.getActiveDriver().getDriver();
    String driverEmail = driver.getEmail();
    Rider rider = ride.getRider();
    String source = ride.getRequestedCarType().getCarCategory();

    issueRequest(driver, rider, source, ride.getId());

    cancelRequest(driver, rider, source);

    assertRideCategoryAndStatus(driverEmail, ride.getRequestedCarType().getCarCategory(), RideUpgradeRequestStatus.CANCELLED);
  }

}
