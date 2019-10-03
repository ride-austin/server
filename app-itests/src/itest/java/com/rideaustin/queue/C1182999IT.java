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
 * Integration test for case: https://testrail.devfactory.com/index.php?/cases/view/1182999
 */
@Category(AirportQueue.class)
public class C1182999IT extends AbstractAirportQueueTest {

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
  @TestCases("C1182999")
  public void test() throws Exception {
    Driver driver = availableActiveDriver.getDriver();

    driverAction
      .locationUpdate(availableActiveDriver, airportLocation.lat, airportLocation.lng)
      .andExpect(status().isOk());

    updateQueue();

    assertEventIsSent(driver, EventType.QUEUED_AREA_ENTERING);

    assertQueuePosition(driver);
  }

}
