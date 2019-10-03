package com.rideaustin.queue;

import javax.inject.Inject;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.rideaustin.model.ride.ActiveDriver;
import com.rideaustin.test.asserts.DriverQueueAssert;
import com.rideaustin.test.fixtures.providers.ActiveDriverFixtureProvider;

import com.rideaustin.test.response.AreaQueuePositions;
import com.rideaustin.test.util.TestUtils;
import com.rideaustin.testrail.TestCases;

@Category(AirportQueue.class)
public class C1183012IT extends AbstractAirportQueueTest {

  @Inject
  private ActiveDriverFixtureProvider provider;

  @Test
  @TestCases("C1183012")
  public void test() throws Exception {
    ActiveDriver queueDriver1 = provider.create().getFixture();
    ActiveDriver queueDriver2 = provider.create().getFixture();
    ActiveDriver cityDriver = provider.create().getFixture();

    driverAction.locationUpdate(queueDriver1, airportLocation.lat, airportLocation.lng);
    updateQueue();

    driverAction.locationUpdate(queueDriver2, airportLocation.lat, airportLocation.lng);
    updateQueue();

    driverAction.locationUpdate(cityDriver, outsideAirportLocation.lat, outsideAirportLocation.lng);
    updateQueue();

    AreaQueuePositions queuePosition = driverAction.getQueueInfo(cityDriver.getDriver().getEmail(), airport.getName());
    DriverQueueAssert.assertThat(queuePosition)
      .hasLength(TestUtils.REGULAR, 2);
  }

}
