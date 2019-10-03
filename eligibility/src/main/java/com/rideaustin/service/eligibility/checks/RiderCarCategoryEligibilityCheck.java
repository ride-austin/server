package com.rideaustin.service.eligibility.checks;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.rideaustin.model.ride.CarType;
import com.rideaustin.model.ride.CityDriverType;
import com.rideaustin.model.ride.DriverType;
import com.rideaustin.model.user.Rider;
import com.rideaustin.service.eligibility.EligibilityCheck;
import com.rideaustin.service.eligibility.EligibilityCheckError;
import com.rideaustin.service.eligibility.RiderEligibilityCheckContext;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@EligibilityCheck(targetClass = Rider.class, contextAware = true)
public class RiderCarCategoryEligibilityCheck extends BaseEligibilityCheckItem<Rider> {

  public RiderCarCategoryEligibilityCheck(Map<String, Object> context) {
    super(context);
  }

  @Override
  public Optional<EligibilityCheckError> check(Rider subject) {
    Long cityId = (Long) context.get(RiderEligibilityCheckContext.CITY);
    List<DriverType> driverTypes = (List<DriverType>) context.get(RiderEligibilityCheckContext.DRIVER_TYPE);
    CarType carType = (CarType) context.get(RiderEligibilityCheckContext.CAR_CATEGORY);
    for (DriverType driverType : driverTypes) {
      CityDriverType cityDriverType = driverType.getCityDriverTypes().stream().filter(cd -> cd.getCityId().equals(cityId)).findFirst().get();
      if ((cityDriverType.getCarCategoriesBitmask() & carType.getBitmask()) == 0) {
        return Optional.of(new EligibilityCheckError(String.format("Rider not eligible to ride %s and %s", carType.getCarCategory(), driverType.getName())));
      }
    }
    return Optional.empty();
  }

}
