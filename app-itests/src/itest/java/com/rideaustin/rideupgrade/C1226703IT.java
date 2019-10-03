package com.rideaustin.rideupgrade;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import javax.inject.Inject;
import javax.inject.Named;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.rideaustin.model.user.Driver;
import com.rideaustin.test.fixtures.SessionFixture;

import com.rideaustin.test.util.TestUtils;
import com.rideaustin.testrail.TestCases;

@Category(RideUpgrade.class)
public class C1226703IT extends AbstractRideUpgradeTest {

  @Inject
  @Named("app310Session")
  private SessionFixture sessionFixture;

  @Override
  @Before
  public void setUp() throws Exception {
    super.setUp();
    rideFixture.setRiderSessionFixture(sessionFixture);
    ride = rideFixture.getFixture();
  }

  @Test
  @TestCases("C1226703")
  public void test() throws Exception {
    Driver driver = ride.getActiveDriver().getDriver();
    String driverEmail = driver.getEmail();

    driverAction
      .requestRideUpgrade(driverEmail, TestUtils.SUV)
      .andExpect(status().isBadRequest())
      .andExpect(content().string("\"Your request can't be submitted to rider, as this rider uses older version of application that doesn't support ride upgrades\""));
  }
}
