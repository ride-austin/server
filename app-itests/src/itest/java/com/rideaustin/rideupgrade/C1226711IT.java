package com.rideaustin.rideupgrade;

import org.junit.Test;

import com.rideaustin.model.user.Driver;
import com.rideaustin.model.user.Rider;
import com.rideaustin.testrail.TestCases;

public class C1226711IT extends AbstractRideUpgradeTest {

  @Test
  @TestCases("C1226711")
  public void test() throws Exception {
    Driver driver = ride.getActiveDriver().getDriver();
    Rider rider = ride.getRider();
    String source = ride.getRequestedCarType().getCarCategory();

    issueRequest(driver, rider, source, ride.getId());

    declineRequest(driver, rider, source);
  }

}
