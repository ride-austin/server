package com.rideaustin.queue;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import javax.inject.Inject;
import javax.inject.Named;

import org.junit.Before;
import org.junit.Test;

import com.rideaustin.model.ride.ActiveDriver;
import com.rideaustin.test.response.AreaQueuePositions;
import com.rideaustin.test.asserts.DriverQueueAssert;
import com.rideaustin.test.fixtures.ActiveDriverFixture;

import com.rideaustin.test.util.TestUtils;
import com.rideaustin.testrail.TestCases;

public class C1183008IT extends AbstractAirportQueueTest {

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
  @TestCases("C1183008")
  public void test() throws Exception {
    driverAction
      .locationUpdate(availableActiveDriver, airportLocation.lat, airportLocation.lng)
      .andExpect(status().isOk());
    updateQueue();
    AreaQueuePositions queuePosition = driverAction.getQueuePosition(availableActiveDriver.getDriver());
    DriverQueueAssert.assertThat(queuePosition)
      .hasPosition(TestUtils.REGULAR, 0)
      .hasLength(TestUtils.REGULAR, 1);

    driverAction
      .locationUpdate(availableActiveDriver, outsideAirportLocation.lat, outsideAirportLocation.lng)
      .andExpect(status().isOk());
    updateQueue();

    queuePosition = driverAction.getQueuePosition(availableActiveDriver.getDriver());
    DriverQueueAssert.assertThat(queuePosition)
      .hasPosition(TestUtils.REGULAR, 0)
      .hasLength(TestUtils.REGULAR, 1);

    driverAction
      .locationUpdate(availableActiveDriver, airportLocation.lat, airportLocation.lng)
      .andExpect(status().isOk());
    updateQueue();

    queuePosition = driverAction.getQueuePosition(availableActiveDriver.getDriver());
    DriverQueueAssert.assertThat(queuePosition)
      .hasPosition(TestUtils.REGULAR, 0)
      .hasLength(TestUtils.REGULAR, 1);

  }
}
