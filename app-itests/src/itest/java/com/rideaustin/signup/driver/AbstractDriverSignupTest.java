package com.rideaustin.signup.driver;

import static com.rideaustin.Constants.City.AUSTIN;
import static com.rideaustin.test.util.TestUtils.RANDOM;

import java.util.Date;

import javax.inject.Inject;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import com.rideaustin.config.AppConfig;
import com.rideaustin.config.RAApplicationInitializer;
import com.rideaustin.config.WebConfig;
import com.rideaustin.model.Address;
import com.rideaustin.model.user.Driver;
import com.rideaustin.model.user.Rider;
import com.rideaustin.test.actions.RiderAction;
import com.rideaustin.test.common.ITestProfileSupport;
import com.rideaustin.test.config.FixtureConfig;
import com.rideaustin.test.config.TestActionsConfig;
import com.rideaustin.test.fixtures.RiderFixture;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {AppConfig.class, WebConfig.class, FixtureConfig.class, TestActionsConfig.class}, initializers = RAApplicationInitializer.class)
@WebAppConfiguration
public abstract class AbstractDriverSignupTest extends ITestProfileSupport {

  @Inject
  protected RiderAction riderAction;

  @Inject
  private RiderFixture riderFixture;
  protected Rider rider;

  @Before
  public void setUp() throws Exception {
    super.setUp();
    rider = riderFixture.getFixture();
  }

  protected Driver createDriver(Rider rider) {
    Driver driver = new Driver();
    driver.setId(RANDOM.nextLong());
    driver.setCityId(AUSTIN.getId());
    driver.setSsn("1234567890");
    driver.setLicenseNumber("456");
    driver.setLicenseState("CA");
    driver.setUser(rider.getUser());
    driver.getUser().setDateOfBirth(new Date());
    driver.getUser().setAddress(new Address());
    driver.getUser().getAddress().setAddress("Test address");
    return driver;
  }
}
