package com.rideaustin.queue;

import static org.junit.Assert.assertEquals;

import java.util.List;

import javax.inject.Inject;

import org.junit.Before;
import org.junit.Test;

import com.rideaustin.model.ride.ActiveDriver;
import com.rideaustin.model.user.Rider;
import com.rideaustin.test.response.AreaQueuePositions;
import com.rideaustin.rest.model.CompactActiveDriverDto;
import com.rideaustin.test.actions.RiderAction;
import com.rideaustin.test.asserts.DriverQueueAssert;
import com.rideaustin.test.fixtures.RiderFixture;
import com.rideaustin.test.fixtures.providers.ActiveDriverFixtureProvider;
import com.rideaustin.test.util.TestUtils;

public class RA12970IT extends AbstractAirportQueueTest {

  @Inject
  private RiderAction riderAction;

  @Inject
  private RiderFixture riderFixture;
  private Rider rider;

  @Inject
  private ActiveDriverFixtureProvider provider;

  @Override
  @Before
  public void setUp() throws Exception {
    super.setUp();
    this.rider = riderFixture.getFixture();
  }

  @Test
  public void test() throws Exception {
    ActiveDriver queueDriver1 = provider.create().getFixture();

    driverAction.locationUpdate(queueDriver1, 30.202772, -97.667991);
    updateQueue();

    AreaQueuePositions queuePosition = driverAction.getQueueInfo(queueDriver1.getDriver().getEmail(), airport.getName());
    DriverQueueAssert.assertThat(queuePosition)
      .hasLength(TestUtils.REGULAR, 1);

    List<CompactActiveDriverDto> result = riderAction.searchDrivers(rider.getEmail(), airportLocation, TestUtils.REGULAR, null);
    assertEquals(1, result.size());
    assertEquals(60L, result.get(0).getDrivingTimeToRider(), 0);
  }
}
