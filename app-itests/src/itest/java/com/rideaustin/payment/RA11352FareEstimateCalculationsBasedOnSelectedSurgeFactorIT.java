package com.rideaustin.payment;

import java.math.BigDecimal;

import javax.inject.Inject;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.test.context.ContextConfiguration;

import com.google.maps.model.LatLng;
import com.rideaustin.config.AppConfig;
import com.rideaustin.config.RAApplicationInitializer;
import com.rideaustin.config.WebConfig;
import com.rideaustin.model.enums.RideStatus;
import com.rideaustin.model.ride.ActiveDriver;
import com.rideaustin.model.ride.Ride;
import com.rideaustin.model.user.Rider;
import com.rideaustin.rest.model.MobileRiderRideDto;
import com.rideaustin.test.AbstractNonTxTests;
import com.rideaustin.test.actions.DriverAction;
import com.rideaustin.test.actions.RiderAction;
import com.rideaustin.test.asserts.EstimateFareAssert;
import com.rideaustin.test.asserts.RiderRideAssert;
import com.rideaustin.test.config.FixtureConfig;
import com.rideaustin.test.config.TestActionsConfig;
import com.rideaustin.test.config.TestSetupConfig;
import com.rideaustin.test.response.EstimateFareDto;
import com.rideaustin.test.setup.RA11352Setup;

@Category(Payment.class)
@ContextConfiguration(classes = {AppConfig.class, WebConfig.class, FixtureConfig.class, TestActionsConfig.class, TestSetupConfig.class}, initializers = RAApplicationInitializer.class)
public class RA11352FareEstimateCalculationsBasedOnSelectedSurgeFactorIT extends AbstractNonTxTests<RA11352Setup> {

  LatLng defaultLocation = new LatLng(30.269372, -97.740394);
  LatLng defaultEndLocation = new LatLng(30.186185, -97.805480);

  @Inject
  private RiderAction riderAction;
  @Inject
  private DriverAction driverAction;

  private RA11352Setup setup;

  @Override
  @Before
  public void setUp() throws Exception {
    super.setUp();
    setup = createSetup();
  }

  @Test
  public void test() throws Exception {

    Rider rider = setup.getRider();

    ActiveDriver firstActiveDriver = setup.getActiveDriver();

    driverAction.locationUpdate(firstActiveDriver, defaultLocation.lat, defaultLocation.lng);

    Long rideId = riderAction.requestRide(rider.getEmail(), defaultLocation, "REGULAR");
    awaitDispatch(firstActiveDriver, rideId);
    driverAction.acceptRide(firstActiveDriver.getDriver().getEmail(), rideId);

    MobileRiderRideDto rideInfo = riderAction.getRideInfo(rider.getEmail(), rideId);

    RiderRideAssert.assertThat(rideInfo)
      .hasStatus(RideStatus.DRIVER_ASSIGNED);

    EstimateFareDto initialEstimation = riderAction.estimateFare(rider.getEmail(), defaultLocation, defaultEndLocation, "REGULAR", 1L);
    updateRideSurgeFactor(rideId, 3);
    EstimateFareDto updatedEstimation = riderAction.estimateFare(rider.getEmail(), defaultLocation, defaultEndLocation, "REGULAR", 1L);
    EstimateFareAssert.assertThat(initialEstimation)
      .surgeFactorUpgradeHasAnEffect(updatedEstimation);
  }

  protected void updateRideSurgeFactor(long rideId, double surgeFactor) {
    Ride ride = rideDslRepository.findOne(rideId);
    ride.setSurgeFactor(BigDecimal.valueOf(surgeFactor));
    rideDslRepository.save(ride);
  }
}
