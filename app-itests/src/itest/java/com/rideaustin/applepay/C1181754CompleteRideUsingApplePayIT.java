package com.rideaustin.applepay;

import static org.junit.Assert.assertNotNull;

import javax.inject.Inject;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.google.maps.model.LatLng;
import com.rideaustin.model.enums.RideStatus;
import com.rideaustin.model.ride.ActiveDriver;
import com.rideaustin.model.user.Driver;
import com.rideaustin.model.user.Rider;
import com.rideaustin.repo.dsl.RideDslRepository;
import com.rideaustin.test.actions.cascaded.CascadedDriverAction;
import com.rideaustin.test.setup.DefaultApplePaySetup;
import com.rideaustin.testrail.TestCases;

/**
 * Apple Pay - Rider should be pre-charged with $1
 */
@Category(ApplePay.class)
public class C1181754CompleteRideUsingApplePayIT extends AbstractApplePayTest<DefaultApplePaySetup> {

  private LatLng defaultLocation = new LatLng(30.269372, -97.740394);
  private LatLng defaultEndLocation = new LatLng(30.186185, -97.805480);

  @Inject
  private RideDslRepository repository;

  @Test
  @TestCases("C1181754")
  public void test() throws Exception {

    Rider rider = setup.getRider();
    ActiveDriver activeDriver = setup.getActiveDriver();
    Driver driver = activeDriver.getDriver();

    CascadedDriverAction cascadedDriverAction = new CascadedDriverAction(driver, driverAction);

    driverAction.goOnline(driver.getUser().getEmail(), defaultLocation);

    Long rideId = riderAction.requestRide(rider.getEmail(), defaultLocation, "REGULAR", null, "sample_token");

    awaitDispatch(activeDriver, rideId);

    cascadedDriverAction.acceptRide(rideId)
      .reach(rideId)
      .startRide(rideId)
      .endRide(rideId, defaultEndLocation.lat, defaultEndLocation.lng);

    awaitStatus(rideId, RideStatus.COMPLETED);

    assertNotNull(repository.findOne(rideId).getPreChargeId());
  }
}
