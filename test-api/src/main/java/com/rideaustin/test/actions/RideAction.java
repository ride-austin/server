package com.rideaustin.test.actions;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.core.env.Environment;
import org.springframework.statemachine.StateMachinePersist;

import com.google.maps.model.LatLng;
import com.rideaustin.Constants;
import com.rideaustin.model.ride.ActiveDriver;
import com.rideaustin.model.user.Rider;
import com.rideaustin.service.model.Events;
import com.rideaustin.service.model.States;
import com.rideaustin.test.AbstractNonTxTests;
import com.rideaustin.test.actions.cascaded.CascadedDriverAction;
import com.rideaustin.test.common.Sleeper;
import com.rideaustin.test.util.TestUtils;

public class RideAction {

  private final RiderAction riderAction;
  private final DriverAction driverAction;
  private final Sleeper sleeper;
  private final StateMachinePersist<States, Events, String> contextAccess;
  private final Environment environment;

  public RideAction(RiderAction riderAction, DriverAction driverAction, Sleeper sleeper, StateMachinePersist<States, Events, String> contextAccess, Environment environment) {
    this.riderAction = riderAction;
    this.driverAction = driverAction;
    this.sleeper = sleeper;
    this.contextAccess = contextAccess;
    this.environment = environment;
  }

  public Long performRide(Rider rider, LatLng pickupLocation, LatLng dropoffLocation, ActiveDriver driver, int rideDuration) throws Exception {
    return performRide(rider, pickupLocation, dropoffLocation, driver, null, TestUtils.REGULAR, rideDuration, Constants.DEFAULT_CITY_ID, false);
  }

  public Long performRide(Rider rider, LatLng pickupLocation, LatLng dropoffLocation, ActiveDriver driver) throws Exception {
    return performRide(rider, pickupLocation, dropoffLocation, driver, TestUtils.REGULAR);
  }

  public Long performRide(Rider rider, LatLng pickupLocation, LatLng dropoffLocation, ActiveDriver driver, boolean useSurgeFare) throws Exception {
    return performRide(rider, pickupLocation, dropoffLocation, driver, null, TestUtils.REGULAR, 0, Constants.DEFAULT_CITY_ID, useSurgeFare);
  }

  public Long performRide(Rider rider, LatLng pickupLocation, LatLng dropoffLocation, ActiveDriver driver, String carCategory) throws Exception {
    return performRide(rider, pickupLocation, dropoffLocation, driver, null, carCategory, 0, Constants.DEFAULT_CITY_ID, false);
  }

  public Long performRide(Rider rider, LatLng pickupLocation, LatLng dropoffLocation, ActiveDriver driver, String applePayToken, String carCategory, final int rideDuration, final Long cityId, final boolean useSurgeFare) throws Exception {
    long dispatchTimeout = 13L;
    driverAction.goOnline(driver.getDriver().getEmail(), pickupLocation, new String[]{carCategory})
      .andExpect(status().isOk());

    Long ride = riderAction.requestRide(rider.getEmail(), pickupLocation, dropoffLocation, carCategory, null, applePayToken, useSurgeFare, cityId);

    AbstractNonTxTests.awaitDispatch(driver, ride, dispatchTimeout, environment, contextAccess);

    CascadedDriverAction cascadedDriverAction = new CascadedDriverAction(driver.getDriver(), driverAction)
      .withRideId(ride);

    cascadedDriverAction
      .acceptRide()
      .reach()
      .startRide();
    if (rideDuration > 0) {
      sleeper.sleep(rideDuration);
    }
    cascadedDriverAction.endRide(dropoffLocation.lat, dropoffLocation.lng);
    return ride;
  }

}
