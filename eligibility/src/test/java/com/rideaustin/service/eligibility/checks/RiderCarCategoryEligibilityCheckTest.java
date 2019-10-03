package com.rideaustin.service.eligibility.checks;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Optional;

import org.junit.Test;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.rideaustin.model.ride.CarType;
import com.rideaustin.model.ride.CityDriverType;
import com.rideaustin.model.ride.DriverType;
import com.rideaustin.model.user.Rider;
import com.rideaustin.service.eligibility.EligibilityCheckError;
import com.rideaustin.service.eligibility.RiderEligibilityCheckContext;

public class RiderCarCategoryEligibilityCheckTest {

  private RiderCarCategoryEligibilityCheck testedInstance;

  @Test
  public void checkRaisesErrorWhenCarTypeIsNotSupportedByDriverType() {
    final long cityId = 1L;
    final DriverType driverType = new DriverType();
    final CityDriverType cityDriverType = new CityDriverType();
    cityDriverType.setCarCategoriesBitmask(2);
    cityDriverType.setCityId(cityId);
    driverType.setCityDriverTypes(ImmutableSet.of(cityDriverType));
    final CarType carType = new CarType();
    carType.setBitmask(1);

    testedInstance = new RiderCarCategoryEligibilityCheck(ImmutableMap.of(
      RiderEligibilityCheckContext.CITY, cityId,
      RiderEligibilityCheckContext.DRIVER_TYPE, ImmutableList.of(driverType),
      RiderEligibilityCheckContext.CAR_CATEGORY, carType
    ));

    final Optional<EligibilityCheckError> result = testedInstance.check(new Rider());

    assertTrue(result.isPresent());
    assertTrue(result.get().getMessage().startsWith("Rider not eligible to ride "));
  }

  @Test
  public void checkRaisesNoErrorWhenCarTypeIsSupportedByDriverType() {
    final long cityId = 1L;
    final DriverType driverType = new DriverType();
    final CityDriverType cityDriverType = new CityDriverType();
    cityDriverType.setCarCategoriesBitmask(1);
    cityDriverType.setCityId(cityId);
    driverType.setCityDriverTypes(ImmutableSet.of(cityDriverType));
    final CarType carType = new CarType();
    carType.setBitmask(1);

    testedInstance = new RiderCarCategoryEligibilityCheck(ImmutableMap.of(
      RiderEligibilityCheckContext.CITY, cityId,
      RiderEligibilityCheckContext.DRIVER_TYPE, ImmutableList.of(driverType),
      RiderEligibilityCheckContext.CAR_CATEGORY, carType
    ));

    final Optional<EligibilityCheckError> result = testedInstance.check(new Rider());

    assertFalse(result.isPresent());
  }


}