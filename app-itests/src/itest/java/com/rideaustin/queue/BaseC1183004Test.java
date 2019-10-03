package com.rideaustin.queue;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import javax.inject.Inject;

import org.junit.Before;
import org.junit.experimental.categories.Category;

import com.rideaustin.model.enums.EventType;
import com.rideaustin.model.ride.ActiveDriver;
import com.rideaustin.model.user.Driver;
import com.rideaustin.service.config.AreaQueueConfig;
import com.rideaustin.test.fixtures.providers.ActiveDriverFixtureProvider;

@Category(AirportQueue.class)
public abstract class BaseC1183004Test extends AbstractAirportQueueTest {

  @Inject
  private ActiveDriverFixtureProvider activeDriverFixtureProvider;
  private ActiveDriver availableActiveDriver;

  @Inject
  private AreaQueueConfig config;

  protected long inactiveTimeout;

  @Override
  @Before
  public void setUp() throws Exception {
    super.setUp();
    availableActiveDriver = activeDriverFixtureProvider.create().getFixture();
    inactiveTimeout = config.getInactiveTimeThresholdBeforeLeave();
  }

  protected void doTest(long timeoutSeconds, EventType eventType) throws Exception {
    Driver driver = availableActiveDriver.getDriver();
    String email = driver.getEmail();

    driverAction
      .locationUpdate(availableActiveDriver, airportLocation.lat, airportLocation.lng)
      .andExpect(status().isOk());
    updateQueue();

    driverAction
      .goOffline(email)
      .andExpect(status().isOk());

    sleeper.sleep(timeoutSeconds * 1000);

    updateQueue();
    assertEventIsSent(driver, eventType);
  }
}
