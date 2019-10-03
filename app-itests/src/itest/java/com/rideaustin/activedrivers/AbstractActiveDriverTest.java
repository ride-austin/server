package com.rideaustin.activedrivers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import com.rideaustin.config.AppConfig;
import com.rideaustin.config.RAApplicationInitializer;
import com.rideaustin.config.WebConfig;
import com.rideaustin.model.ride.ActiveDriver;
import com.rideaustin.model.ride.Ride;
import com.rideaustin.test.actions.AdministratorAction;
import com.rideaustin.test.actions.DriverAction;
import com.rideaustin.test.actions.RiderAction;
import com.rideaustin.test.common.ITestProfileSupport;
import com.rideaustin.test.config.FixtureConfig;
import com.rideaustin.test.config.TestActionsConfig;
import com.rideaustin.test.fixtures.AdministratorFixture;
import com.rideaustin.test.fixtures.CarFixture;
import com.rideaustin.test.fixtures.DriverFixture;
import com.rideaustin.test.fixtures.RiderFixture;
import com.rideaustin.test.fixtures.SessionFixture;
import com.rideaustin.test.fixtures.providers.ActiveDriverFixtureProvider;
import com.rideaustin.test.fixtures.providers.DriverFixtureProvider;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {AppConfig.class, WebConfig.class, FixtureConfig.class, TestActionsConfig.class}, initializers = RAApplicationInitializer.class)
@WebAppConfiguration
public abstract class AbstractActiveDriverTest extends ITestProfileSupport {

  @Inject
  @Named("simpleRiderWithCharity")
  protected RiderFixture riderfixture;

  @Inject
  protected AdministratorFixture administratorFixture;

  @Inject
  protected ActiveDriverFixtureProvider activeDriverFixtureProvider;

  @Inject
  protected DriverFixtureProvider driverFixtureProvider;

  @Inject
  @Qualifier("suvCar")
  protected CarFixture carFixture;

  @Inject
  @Qualifier("luxuryCar")
  protected CarFixture luxuryCarFixture;

  @Inject
  protected RiderAction riderAction;

  @Inject
  @Named("simpleDriver")
  protected DriverFixture driverFixture;
  protected Ride ride;

  @Inject
  @Named("app320Session")
  private SessionFixture sessionFixture;

  @Inject
  protected DriverAction driverAction;
  @Inject
  protected AdministratorAction administratorAction;

  protected void assertContentIsCorrect(String content) {
    if (!content.contains("\"content\":[{\"latitude\":30.202596,\"longitude\":-97.667001,\"carCategories\"")) {
      throw new AssertionError("Illegal content of loading AD for ADMIN");
    }
  }

  public void assertNumberOfActiveDriver(int number) {
    List<Map<String, Object>> ad = jdbcTemplate.queryForList("SELECT * FROM active_drivers");
    assertThat(ad.size(), is(equalTo(number)));

  }

  protected void assertCorrectDriver(String content, ActiveDriver ad) {
    if (!content.contains("\"driver\":{\"id\":" + ad.getDriver().getId() + ",\"user\":{\"id\":" + ad.getDriver().getUser().getId() + "}}")) {
      throw new AssertionError("Illegal content of loading AD for ADMIN");
    }
  }
}
