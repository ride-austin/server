package com.rideaustin.queue;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;

import javax.inject.Inject;
import javax.inject.Named;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.jayway.awaitility.Awaitility;
import com.rideaustin.model.enums.EventType;
import com.rideaustin.model.ride.ActiveDriver;
import com.rideaustin.model.user.Driver;
import com.rideaustin.service.config.AreaQueueConfig;
import com.rideaustin.test.fixtures.ActiveDriverFixture;
import com.rideaustin.testrail.TestCases;

@Category(AirportQueue.class)
public class C1183003IT extends AbstractAirportQueueTest {

  @Inject
  @Named("availableActiveDriver")
  private ActiveDriverFixture activeDriverFixture;
  private ActiveDriver availableActiveDriver;

  @Inject
  private AreaQueueConfig config;

  private long awayTimeout;

  @Override
  @Before
  public void setUp() throws Exception {
    super.setUp();
    availableActiveDriver = activeDriverFixture.getFixture();
    awayTimeout = config.getOutOfAreaTimeThresholdBeforeLeave();
  }

  @Test
  @TestCases("C1183003")
  public void testLeave() throws Exception {
    doTest(awayTimeout * 60 + 1, EventType.QUEUED_AREA_LEAVING);
  }

  @Test
  @TestCases("C1183003")
  public void testStay() throws Exception {
    doTest(awayTimeout * 60 - 1, EventType.QUEUED_AREA_ENTERING);
  }

  private void doTest(long timeoutSeconds, EventType eventType) throws Exception {
    Driver driver = availableActiveDriver.getDriver();
    Instant start = Instant.now();

    driverAction
      .locationUpdate(availableActiveDriver, airportLocation.lat, airportLocation.lng)
      .andExpect(status().isOk());
    updateQueue();

    driverAction
      .locationUpdate(availableActiveDriver, outsideAirportLocation.lat, outsideAirportLocation.lng)
      .andExpect(status().isOk());
    updateQueue();

    Awaitility.await().forever().until(
      () -> Instant.now().getEpochSecond() - start.getEpochSecond() > timeoutSeconds
    );

    driverAction
      .locationUpdate(availableActiveDriver, outsideAirportLocation.lat, outsideAirportLocation.lng)
      .andExpect(status().isOk());
    updateQueue();
    assertEventIsSent(driver, eventType);
  }
}
