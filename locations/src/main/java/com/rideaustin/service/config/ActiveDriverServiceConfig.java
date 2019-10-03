package com.rideaustin.service.config;

import javax.inject.Inject;

import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.rideaustin.Constants;
import com.rideaustin.utils.GeometryUtils;
import com.sromku.polygon.Polygon;

@Component
public class ActiveDriverServiceConfig {

  private static final int DEFAULT_NUMBER_OF_ETA_DRIVERS = 3;

  private final Integer numberOfEtaDrivers;
  private final Integer maxActiveDriverAvatars;
  private final Integer driverMaxEtaTime;
  private final Integer driverCityCenterMaxEtaTime;
  private final Polygon cityCenterDispatchPolygon;
  private final Integer driverMaxDeclinedRequests;
  private final Integer fingerprintedDriverHandicap;

  @Inject
  public ActiveDriverServiceConfig(Environment environment) {
    numberOfEtaDrivers = environment.getProperty("dispatch.number_of_eta_calculations", Integer.class,
      DEFAULT_NUMBER_OF_ETA_DRIVERS);
    maxActiveDriverAvatars = environment.getProperty("active_driver.max_displayed_avatars",
      Integer.class, Constants.CLOSEST_ACDR_SIZE);

    driverMaxEtaTime = environment.getProperty("dispatch.driver_max_eta.limit", Integer.class, Integer.MAX_VALUE);
    driverCityCenterMaxEtaTime = environment.getProperty("dispatch.driver_city_center_max_eta.limit", Integer.class, 900);
    cityCenterDispatchPolygon = GeometryUtils.buildPolygon(environment.getProperty("dispatch.city_center_area_csv", String.class));
    driverMaxDeclinedRequests = environment.getProperty("active_driver.max_declined_requests", Integer.class, -1);
    fingerprintedDriverHandicap = environment.getProperty("dispatch.driver.fingerprinted.handicap", Integer.class, 120);
  }

  public Integer getNumberOfEtaDrivers() {
    return numberOfEtaDrivers;
  }

  public Integer getMaxActiveDriverAvatars() {
    return maxActiveDriverAvatars;
  }

  public Integer getDriverMaxEtaTime() {
    return driverMaxEtaTime;
  }

  public Integer getDriverCityCenterMaxEtaTime() {
    return driverCityCenterMaxEtaTime;
  }

  public Polygon getCityCenterDispatchPolygon() {
    return cityCenterDispatchPolygon;
  }

  public Integer getDriverMaxDeclinedRequests() {
    return driverMaxDeclinedRequests;
  }

  public Integer getFingerprintedDriverHandicap() {
    return fingerprintedDriverHandicap;
  }
}
