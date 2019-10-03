package com.rideaustin.activedrivers;

import static org.junit.Assert.assertEquals;

import java.util.List;

import javax.inject.Inject;

import org.junit.Test;

import com.rideaustin.model.enums.ActiveDriverStatus;
import com.rideaustin.model.ride.ActiveDriver;
import com.rideaustin.service.ActiveDriverLocationService;
import com.rideaustin.service.model.OnlineDriverDto;

public class RA207AwayIT extends AbstractActiveDriverTest {

  @Inject
  private ActiveDriverLocationService activeDriverLocationService;

  @Test
  public void test() throws Exception {
    ActiveDriver activeDriver = activeDriverFixtureProvider.create().getFixture();
    driverAction.goOnline(activeDriver.getDriver().getEmail(), 30.202596, -97.667001);
    activeDriverLocationService.updateActiveDriverLocationStatus(activeDriver.getId(), ActiveDriverStatus.AWAY);

    driverAction.locationUpdate(activeDriver, 30.202596, -97.667001);

    final List<OnlineDriverDto> drivers = activeDriverLocationService.getActiveDriversByStatus(ActiveDriverStatus.AVAILABLE);
    assertEquals(1, drivers.size());
  }
}
