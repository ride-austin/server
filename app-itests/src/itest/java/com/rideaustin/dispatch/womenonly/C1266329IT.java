package com.rideaustin.dispatch.womenonly;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.rideaustin.model.enums.RideStatus;
import com.rideaustin.model.ride.DriverType;
import com.rideaustin.test.actions.cascaded.CascadedDriverAction;
import com.rideaustin.test.asserts.DispatchHistoryAssert;
import com.rideaustin.test.setup.C1266329Setup;
import com.rideaustin.test.util.TestUtils;
import com.rideaustin.testrail.TestCases;

@Category(WomenOnly.class)
public class C1266329IT extends AbstractWomenOnlyDispatchTest<C1266329Setup> {

  @Override
  @Before
  public void setUp() throws Exception {
    super.setUp();
  }

  @Test
  @TestCases("C1266329")
  public void testAllTypesEnabled() throws Exception {
    String[] carCategories = {
      TestUtils.REGULAR, TestUtils.SUV, TestUtils.PREMIUM
    };
    driverAction.goOnline(woDriver.getDriver().getEmail(), AUSTIN_CENTER);
    driverAction.locationUpdate(woDriver, AUSTIN_CENTER.lat, AUSTIN_CENTER.lng,
      carCategories, new String[]{DriverType.WOMEN_ONLY})
      .andExpect(status().isOk());

    for (String carCategory : carCategories) {
      requestRideAndCancelByDriver(carCategory);
    }
  }

  @Test
  @TestCases("C1266329")
  public void testRegularDisabled() throws Exception {
    doTestDisabled(TestUtils.REGULAR, new String[]{
      TestUtils.SUV, TestUtils.PREMIUM
    });
  }

  @Test
  @TestCases("C1266329")
  public void testSuvDisabled() throws Exception {
    doTestDisabled(TestUtils.SUV, new String[]{
      TestUtils.REGULAR, TestUtils.PREMIUM
    });
  }

  @Test
  @TestCases("C1266329")
  public void testPremiumDisabled() throws Exception {
    doTestDisabled(TestUtils.PREMIUM, new String[]{
      TestUtils.REGULAR, TestUtils.SUV
    });
  }

  private void doTestDisabled(String excluded, String[] available) throws Exception {
    driverAction.goOnline(woDriver.getDriver().getEmail(), AUSTIN_CENTER);
    driverAction.locationUpdate(woDriver, AUSTIN_CENTER.lat, AUSTIN_CENTER.lng,
      available, new String[]{DriverType.WOMEN_ONLY})
      .andExpect(status().isOk());

    Long ride = riderAction.requestRide(rider.getEmail(), AUSTIN_CENTER, excluded, DriverType.WOMEN_ONLY);

    awaitStatus(ride, RideStatus.NO_AVAILABLE_DRIVER);

    DispatchHistoryAssert.assertThat(getDispatchHistory(ride))
      .isEmpty();
  }

  private void requestRideAndCancelByDriver(String category) throws Exception {
    Long ride = riderAction.requestRide(rider.getEmail(), AUSTIN_CENTER, category, DriverType.WOMEN_ONLY);

    awaitDispatch(woDriver, ride);

    CascadedDriverAction cascadedDriverAction = new CascadedDriverAction(woDriver.getDriver(), driverAction)
      .withRideId(ride);
    cascadedDriverAction.acceptRide().reach();

    awaitStatus(ride, RideStatus.DRIVER_REACHED);

    cascadedDriverAction.cancelRide();
  }
}
