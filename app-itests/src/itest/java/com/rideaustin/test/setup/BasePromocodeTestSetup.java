package com.rideaustin.test.setup;

import java.math.BigDecimal;
import java.util.Optional;

import javax.inject.Inject;

import com.rideaustin.model.ride.CarType;
import com.rideaustin.model.ride.CityCarType;
import com.rideaustin.service.CarTypeService;

public abstract class BasePromocodeTestSetup<T> implements SetupAction<T> {

  @Inject
  private CarTypeService carTypeService;

  public BigDecimal getMinimumFareForCityCarType(Long cityId, CarType carType) {
    Optional<CityCarType> cityCarType = carTypeService.getCityCarType(carType, cityId);
    if (cityCarType.isPresent()) {
      return cityCarType.get().getMinimumFare().getAmount();
    } else {
      throw new AssertionError(String.format("Should have found car type %s for %s", carType.getCarCategory(), cityId));
    }
  }
}
