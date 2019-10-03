package com.rideaustin.test.actions.cascaded;

import java.math.BigDecimal;

import com.rideaustin.model.ride.ActiveDriver;
import com.rideaustin.model.user.Driver;
import com.rideaustin.test.actions.DriverAction;

public class CascadedDriverAction {
  private final Driver driver;

  private final DriverAction driverAction;

  private ResultVerificationState resultVerificationState = new SuccessVerificationState();

  private Long rideId;

  public CascadedDriverAction(Driver driver, DriverAction driverAction) {
    this.driver = driver;
    this.driverAction = driverAction;
  }

  public CascadedDriverAction withRideId(Long rideId) {
    this.rideId = rideId;
    return this;
  }

  public CascadedDriverAction withVerification(ResultVerificationState state) {
    this.resultVerificationState = state;
    return this;
  }

  public CascadedDriverAction goOnline(double lat, double lng) throws Exception {
    resultVerificationState.verify(driverAction.goOnline(driver.getEmail(), lat, lng));
    return this;
  }

  public CascadedDriverAction goOffline() throws Exception {
    resultVerificationState.verify(driverAction.goOffline(driver.getEmail()));
    return this;
  }

  public CascadedDriverAction locationUpdate(ActiveDriver activeDriver, double lat, double lng) throws Exception {
    resultVerificationState.verify(driverAction.locationUpdate(activeDriver, lat, lng));
    return this;
  }

  public CascadedDriverAction locationUpdate(ActiveDriver activeDriver, double lat, double lng, String[] carCategories) throws Exception {
    resultVerificationState.verify(driverAction.locationUpdate(activeDriver, lat, lng, carCategories));
    return this;
  }

  public CascadedDriverAction locationUpdate(ActiveDriver activeDriver, double lat, double lng, String[] carCategories, String[] driverTypes) throws Exception {
    resultVerificationState.verify(driverAction.locationUpdate(activeDriver, lat, lng, carCategories, driverTypes));
    return this;
  }

  public CascadedDriverAction endRide(double lat, double lng) throws Exception {
    return endRide(rideId, lat, lng);
  }

  public CascadedDriverAction endRide(long rideId, double lat, double lng) throws Exception {
    resultVerificationState.verify(driverAction.endRide(driver.getEmail(), rideId, lat, lng));
    return this;
  }

  public CascadedDriverAction startRide() throws Exception {
    return startRide(rideId);
  }

  public CascadedDriverAction startRide(long rideId) throws Exception {
    resultVerificationState.verify(driverAction.startRide(driver.getEmail(), rideId));
    return this;
  }

  public CascadedDriverAction acceptRide() throws Exception {
    return acceptRide(rideId);
  }

  public CascadedDriverAction acceptRide(long rideId) throws Exception {
    resultVerificationState.verify(driverAction.acceptRide(driver.getEmail(), rideId));
    return this;
  }

  public CascadedDriverAction acceptRide(ActiveDriver activeDriver, long rideId) throws Exception {
    resultVerificationState.verify(driverAction.acceptRide(activeDriver, rideId));
    return this;
  }

  public CascadedDriverAction reach() throws Exception {
    return reach(rideId);
  }

  public CascadedDriverAction reach(long rideId) throws Exception {
    resultVerificationState.verify(driverAction.reach(driver.getEmail(), rideId));
    return this;
  }

  public CascadedDriverAction declineRide() throws Exception {
    return declineRide(rideId);
  }

  public CascadedDriverAction declineRide(long rideId) throws Exception {
    resultVerificationState.verify(driverAction.declineRide(driver.getEmail(), rideId));
    return this;
  }

  public CascadedDriverAction declineRide(ActiveDriver activeDriver, long rideId) throws Exception {
    resultVerificationState.verify(driverAction.declineRide(activeDriver, rideId));
    return this;
  }

  public CascadedDriverAction cancelRide() throws Exception {
    return cancelRide(rideId);
  }

  public CascadedDriverAction cancelRide(long rideId) throws Exception {
    resultVerificationState.verify(driverAction.cancelRide(driver.getEmail(), rideId));
    return this;
  }

  public CascadedDriverAction rateRide(BigDecimal rating) throws Exception {
    return rateRide(rideId, rating);
  }

  public CascadedDriverAction rateRide(long rideId, BigDecimal rating) throws Exception {
    resultVerificationState.verify(driverAction.rateRide(driver.getEmail(), rideId, rating));
    return this;
  }

  public CascadedDriverAction requestRideUpgrade(String target) throws Exception {
    resultVerificationState.verify(driverAction.requestRideUpgrade(driver.getEmail(), target));
    return this;
  }

  public CascadedDriverAction cancelRideUpgrade() throws Exception {
    resultVerificationState.verify(driverAction.cancelRideUpgrade(driver.getEmail()));
    return this;
  }
}
