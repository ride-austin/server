package com.rideaustin.rideupgrade;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import javax.inject.Inject;
import javax.inject.Named;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.rideaustin.model.ride.Ride;
import com.rideaustin.model.user.Driver;
import com.rideaustin.test.fixtures.RideFixture;

import com.rideaustin.test.util.TestUtils;
import com.rideaustin.testrail.TestCases;

@Category(RideUpgrade.class)
public class C1226753IT extends AbstractRideUpgradeTest {

  @Inject
  @Named("riderCancelledRide")
  private RideFixture rideFixture;
  private Ride ride;

  @Override
  @Before
  public void setUp() throws Exception {
    super.setUp();
    ride = rideFixture.getFixture();
  }

  @Test
  @TestCases("C1226753")
  public void test() throws Exception {
    Driver driver = ride.getActiveDriver().getDriver();
    String driverEmail = driver.getEmail();

    driverAction
      .requestRideUpgrade(driverEmail, TestUtils.SUV)
      .andExpect(status().isBadRequest())
      .andExpect(content().string("\"You can't request ride upgrade while not in a ride\""));
  }
}
