package com.rideaustin.service.eligibility;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import com.rideaustin.model.ride.ActiveDriver;
import com.rideaustin.model.ride.Car;
import com.rideaustin.model.user.Driver;

import lombok.Getter;

@Getter
public class DriverEligibilityCheckContext extends BaseEligibilityCheckContext {

  public static final String DRIVER_TYPES = "driverTypes";
  public static final String CAR_CATEGORIES = "carCategories";
  public static final String CITY = "city";

  private final Driver driver;
  private final ActiveDriver activeDriver;
  private final Car car;

  public DriverEligibilityCheckContext(Driver driver, ActiveDriver activeDriver, Car car, Map<String, Object> params) {
    super(params, Collections.emptySet());
    this.driver = driver;
    this.activeDriver = activeDriver;
    this.car = car;
  }

  public DriverEligibilityCheckContext(Driver driver, ActiveDriver activeDriver, Car car, Map<String, Object> params, Set<Class<?>> requestedChecks) {
    super(params, requestedChecks);
    this.driver = driver;
    this.activeDriver = activeDriver;
    this.car = car;
  }

}
