package com.rideaustin.queue;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import javax.inject.Inject;
import javax.inject.Named;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.rideaustin.model.enums.EventType;
import com.rideaustin.model.ride.ActiveDriver;
import com.rideaustin.model.user.Driver;
import com.rideaustin.test.fixtures.ActiveDriverFixture;
import com.rideaustin.testrail.TestCases;

/**
 * Integration test for case https://testrail.devfactory.com/index.php?/cases/view/1183000
 */
@Category(AirportQueue.class)
public class C1183000IT extends AbstractAirportQueueTest {

  @Inject
  @Named("availableActiveDriver")
  private ActiveDriverFixture activeDriverFixture;
  private ActiveDriver availableActiveDriver;

  @Override
  @Before
  public void setUp() throws Exception {
    super.setUp();
    availableActiveDriver = activeDriverFixture.getFixture();
  }

  @Test
  @TestCases("C1183000")
  public void test() throws Exception {
    Driver driver = availableActiveDriver.getDriver();
    String login = driver.getUser().getEmail();
    driverAction
      .goOnline(login, airportLocation)
      .andExpect(status().isOk());

    updateQueue();

    assertEventIsSent(driver, EventType.QUEUED_AREA_ENTERING);

    assertQueuePosition(driver);
  }
}
