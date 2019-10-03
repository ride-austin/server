package com.rideaustin.service.config;

import javax.inject.Inject;

import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.rideaustin.Constants;
import com.rideaustin.utils.GeometryUtils;
import com.sromku.polygon.Polygon;

@Component
public class RideDispatchServiceConfig {

  private final int driverSearchRadiusLimit;
  private final int driverSearchRadiusStep;
  private final int driverSearchRadiusStart;
  private final int driverMaxDeclinedRequests;
  private final int rideRequestDeliveryTimeout;
  private final int driverMaxEtaTime;
  private final int driverCityCenterMaxEtaTime;
  private final long inceptionMachineExpiration;
  private final Polygon cityCenterDispatchPolygon;
  private final RideAcceptanceConfig rideAcceptanceConfig;
  private final long rideQueueExpirationTimeout;

  @Inject
  public RideDispatchServiceConfig(Environment env, RideAcceptanceConfig rideAcceptanceConfig) {
    rideRequestDeliveryTimeout = env.getProperty("dispatch.rideRequestDeliveryTimeout", Integer.class, 4);
    driverSearchRadiusStart = env.getProperty("dispatch.driver_search_radius.start", Integer.class, Constants.SQUARE_MILE_DIMENSION_MILES);
    driverSearchRadiusLimit = env.getProperty("dispatch.driver_search_radius.limit", Integer.class,
      Constants.SQUARE_MILE_DIMENSION_MILES);
    driverSearchRadiusStep = env.getProperty("dispatch.driver_search_radius.step", Integer.class, 0);
    driverMaxDeclinedRequests = env.getProperty("active_driver.max_declined_requests", Integer.class, -1);
    driverMaxEtaTime = env.getProperty("dispatch.driver_max_eta.limit", Integer.class, Integer.MAX_VALUE);
    driverCityCenterMaxEtaTime = env.getProperty("dispatch.driver_city_center_max_eta.limit", Integer.class, 900);
    cityCenterDispatchPolygon = GeometryUtils.buildPolygon(env.getProperty("dispatch.city_center_area_csv", String.class));
    inceptionMachineExpiration = env.getProperty("dispatch.inception.machine.expiration", Long.class, 21_600_000L);
    rideQueueExpirationTimeout = env.getProperty("dispatch.ride.queue.expiration", Long.class, 600L);
    this.rideAcceptanceConfig = rideAcceptanceConfig;
  }

  public int getTotalDispatchWaitTime(Long cityId) {
    return rideAcceptanceConfig.getTotalWaitTime(cityId);
  }

  public int getTotalDispatchWaitTime() {
    return getTotalDispatchWaitTime(Constants.DEFAULT_CITY_ID);
  }

  public int getPerDriverWaitTime(Long cityId) {
    return rideAcceptanceConfig.getPerDriverWaitPeriod(cityId);
  }

  public int getPerDriverWaitTime() {
    return getPerDriverWaitTime(Constants.DEFAULT_CITY_ID);
  }

  public int getDriverSearchRadiusLimit() {
    return driverSearchRadiusLimit;
  }

  public int getDriverSearchRadiusStep() {
    return driverSearchRadiusStep;
  }

  public int getDriverMaxDeclinedRequests() {
    return driverMaxDeclinedRequests;
  }

  public int getRideRequestDeliveryTimeout() {
    return rideRequestDeliveryTimeout;
  }

  public int getDriverMaxEtaTime() {
    return driverMaxEtaTime;
  }

  public Polygon getCityCenterDispatchPolygon() {
    return cityCenterDispatchPolygon;
  }

  public int getDriverCityCenterMaxEtaTime() {
    return driverCityCenterMaxEtaTime;
  }

  public int getDispatchAcceptanceTimeout(Long cityId) {
    return rideAcceptanceConfig.getDriverAcceptancePeriod(cityId);
  }

  public int getDispatchAcceptanceTimeout() {
    return getDispatchAcceptanceTimeout(Constants.DEFAULT_CITY_ID);
  }

  public int getDispatchAllowanceTimeout(Long cityId) {
    return rideAcceptanceConfig.getAllowancePeriod(cityId);
  }

  public int getDispatchAllowanceTimeoutWithCoverage(Long cityId) {
    return rideAcceptanceConfig.getAllowancePeriod(cityId) + rideAcceptanceConfig.getNetworkLatencyCoverage(cityId);
  }

  public int getDispatchAllowanceTimeoutWithCoverage() {
    return getDispatchAllowanceTimeoutWithCoverage(Constants.DEFAULT_CITY_ID);
  }

  public long getInceptionMachineExpiration() {
    return inceptionMachineExpiration;
  }

  public int getDriverSearchRadiusStart() {
    return driverSearchRadiusStart;
  }

  public long getRideQueueExpirationTimeout() {
    return rideQueueExpirationTimeout;
  }
}
