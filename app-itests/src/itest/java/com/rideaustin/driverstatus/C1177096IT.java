package com.rideaustin.driverstatus;

import static org.assertj.core.api.Assertions.assertThat;

import javax.inject.Inject;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.rideaustin.model.ride.ActiveDriver;
import com.rideaustin.model.user.Rider;
import com.rideaustin.service.ActiveDriverLocationService;
import com.rideaustin.service.location.enums.LocationType;
import com.rideaustin.service.model.OnlineDriverDto;
import com.rideaustin.test.actions.cascaded.CascadedDriverAction;
import com.rideaustin.test.setup.C1177095Setup;
import com.rideaustin.test.util.TestUtils;
import com.rideaustin.testrail.TestCases;

@Category(DriverStatusManagement.class)
public class C1177096IT extends AbstractNonTxDriverStatusManagementTest<C1177095Setup> {

  @Inject
  private ActiveDriverLocationService activeDriverLocationService;

  @Override
  @Before
  public void setUp() throws Exception {
    super.setUp();
    setup = createSetup();
  }

  @Test
  @TestCases("C1177096")
  public void shouldGoOffline_WhenDeclinedWO() throws Exception {
    ActiveDriver woDriver = setup.getActiveDriver();
    CascadedDriverAction cascadedAction = new CascadedDriverAction(woDriver.getDriver(), driverAction);
    String womenOnly = "WOMEN_ONLY";
    String carType = TestUtils.PREMIUM;
    cascadedAction.locationUpdate(woDriver, austinCenter.lat, austinCenter.lng, new String[]{carType}, new String[]{womenOnly});

    Rider rider = setup.getRider();
    for (int i = 0; i < declinedRidesLimit; i++) {
      Long ride = riderAction.requestRide(rider.getEmail(), austinCenter, carType, womenOnly);
      awaitDispatch(woDriver, ride);
      cascadedAction
        .withRideId(ride)
        .declineRide();
      sleeper.sleep(100L);
    }

    OnlineDriverDto updatedActiveDriver = activeDriverLocationService.getById(woDriver.getId(), LocationType.ACTIVE_DRIVER);
    assertThat(updatedActiveDriver).isNull();
  }
}
