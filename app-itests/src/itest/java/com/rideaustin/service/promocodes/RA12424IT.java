package com.rideaustin.service.promocodes;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Collections;

import javax.inject.Inject;

import org.junit.Before;
import org.junit.Test;

import com.google.maps.model.LatLng;
import com.rideaustin.model.enums.RideStatus;
import com.rideaustin.model.user.Rider;
import com.rideaustin.rest.model.MobileRiderRideDto;
import com.rideaustin.service.RideSummaryService;
import com.rideaustin.test.actions.cascaded.CascadedDriverAction;
import com.rideaustin.test.setup.RA12424Setup;
import com.rideaustin.test.util.TestUtils;

public class RA12424IT extends AbstractSplitFarePromocodeApplicableToFeesTest<RA12424Setup> {

  private Rider secondaryRider;

  @Inject
  private RideSummaryService rideSummaryService;

  @Override
  @Before
  public void setUp() throws Exception {
    super.setUp();
    secondaryRider = setup.getSecondaryRider();
  }

  @Test
  public void test() throws Exception {
    LatLng center = locationProvider.getCenter();
    riderAction.usePromocode(rider, promocode)
      .andExpect(status().isOk());
    driverAction.locationUpdate(activeDriver, center.lat, center.lng, new String[]{TestUtils.PREMIUM})
      .andExpect(status().isOk());
    Long ride = riderAction.requestRide(rider.getEmail(), center, TestUtils.PREMIUM);

    awaitDispatch(activeDriver, ride);

    CascadedDriverAction cascadedDriverAction = new CascadedDriverAction(activeDriver.getDriver(), driverAction)
      .withRideId(ride);

    cascadedDriverAction.acceptRide();

    Long splitFare = riderAction.requestSplitFare(rider.getEmail(), ride, Collections.singleton(secondaryRider.getPhoneNumber()));
    riderAction.acceptSplitFare(secondaryRider.getEmail(), splitFare)
      .andExpect(status().isOk());

    cascadedDriverAction.reach()
      .startRide()
      .endRide(center.lat, center.lng);

    awaitStatus(ride, RideStatus.COMPLETED);

    rideSummaryService.completeRide(ride);

    MobileRiderRideDto rideInfo = riderAction.getRideInfo(rider.getEmail(), ride);

    System.out.println(rideInfo);
  }
}
