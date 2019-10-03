package com.rideaustin.service.promocodes;

import com.google.maps.model.LatLng;
import com.rideaustin.Constants;
import com.rideaustin.filter.ClientType;
import com.rideaustin.model.enums.RideStatus;
import com.rideaustin.model.promocodes.PromocodeRedemption;
import com.rideaustin.model.ride.ActiveDriver;
import com.rideaustin.model.ride.Ride;
import com.rideaustin.model.user.Rider;
import com.rideaustin.test.actions.cascaded.CascadedDriverAction;
import com.rideaustin.test.setup.C1177043Setup;
import com.rideaustin.test.util.TestUtils;
import com.rideaustin.testrail.TestCases;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.math.BigDecimal;

import static com.rideaustin.service.promocodes.AbstractPromocodeTest.assertNotUsed;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Category(RiderPromocode.class)
public class C1177043IT extends AbstractNonTxPromocodeTest<C1177043Setup> {

  private ActiveDriver activeDriver;
  private Rider rider;

  private PromocodeRedemption redemption;

  private CascadedDriverAction cascadedDriverAction;

  @Before
  @Override
  public void setUp() throws Exception {
    super.setUp();
    this.setup = createSetup();
    this.activeDriver = setup.getActiveDriver();
    this.redemption = setup.getRedemption();
    this.rider = setup.getRider();
    this.cascadedDriverAction = new CascadedDriverAction(activeDriver.getDriver(), driverAction);
    configurationItemCache.setIntConfig(Constants.DEFAULT_CITY_ID, ClientType.CONSOLE, "ridePayment", "cancellationChargeFreePeriod", 20);
  }

  @Test
  @TestCases("C1177043")
  public void shouldNotApplyPromocode() throws Exception {
    LatLng location = locationProvider.getCenter();
    driverAction.goOnline(activeDriver.getDriver().getEmail(), location, new String[]{TestUtils.REGULAR, TestUtils.SUV})
      .andExpect(status().isOk());
    driverAction.locationUpdate(activeDriver, location.lat, location.lng, new String[]{TestUtils.REGULAR, TestUtils.SUV});

    // Second ride
    Long secondRide = riderAction.requestRide(rider.getEmail(), location, TestUtils.SUV);
    awaitDispatch(activeDriver, secondRide);
    cascadedDriverAction.withRideId(secondRide)
      .acceptRide()
      .reach()
      .startRide()
      .endRide(location.lat, location.lng);
    awaitStatus(secondRide, RideStatus.COMPLETED);

    Ride secondRideEntity = rideDslRepository.findOne(secondRide);
    assertNotUsed(secondRideEntity, redemption, 0);

    // Third ride
    Long thirdRide = riderAction.requestRide(rider.getEmail(), location, TestUtils.SUV);
    awaitDispatch(activeDriver, thirdRide);
    cascadedDriverAction.withRideId(thirdRide)
      .acceptRide();
    awaitStatus(thirdRide, RideStatus.DRIVER_ASSIGNED);

    sleeper.sleep(30000);

    riderAction.cancelRide(rider.getEmail(), thirdRide)
      .andExpect(status().isOk());
    awaitStatus(thirdRide, RideStatus.RIDER_CANCELLED);

    Ride thirdRideEntity = rideDslRepository.findOne(thirdRide);
    assertThat(thirdRideEntity.getCancellationFee().getAmount()).isGreaterThan(BigDecimal.ZERO);
    assertNotUsed(thirdRideEntity, redemption, 0);
  }

}
