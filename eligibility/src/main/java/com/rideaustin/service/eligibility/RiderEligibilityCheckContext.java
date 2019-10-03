package com.rideaustin.service.eligibility;

import java.util.Collections;
import java.util.Map;

import com.rideaustin.model.user.Rider;

import lombok.Getter;

@Getter
public class RiderEligibilityCheckContext extends BaseEligibilityCheckContext {

  public static final String DRIVER_TYPE = "driverType";
  public static final String CAR_CATEGORY = "carCategory";
  public static final String CITY = "city";

  private final Rider rider;

  public RiderEligibilityCheckContext(Rider rider, Map<String, Object> params) {
    super(params, Collections.emptySet());
    this.rider = rider;
  }

}
