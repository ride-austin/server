package com.rideaustin.rideupgrade;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.rideaustin.model.user.Driver;
import com.rideaustin.model.user.Rider;
import com.rideaustin.testrail.TestCases;

@Category(RideUpgrade.class)
public class C1226705IT extends AbstractRideUpgradeTest {

  @Test
  @TestCases("C1226705")
  public void test() throws Exception {
    Driver driver = ride.getActiveDriver().getDriver();
    Rider rider = ride.getRider();
    String source = ride.getRequestedCarType().getCarCategory();

    issueRequest(driver, rider, source, ride.getId());

    cancelRequest(driver, rider, source);

    acceptRequest(driver, rider, source, ride.getId());
  }

}
