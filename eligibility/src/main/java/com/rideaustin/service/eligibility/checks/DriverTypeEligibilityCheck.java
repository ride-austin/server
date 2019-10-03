package com.rideaustin.service.eligibility.checks;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.inject.Inject;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.rideaustin.model.ride.ActiveDriver;
import com.rideaustin.service.eligibility.EligibilityCheck;
import com.rideaustin.service.eligibility.DriverEligibilityCheckContext;
import com.rideaustin.service.eligibility.EligibilityCheckError;
import com.rideaustin.service.ride.DriverTypeService;
import com.rideaustin.service.user.DriverTypeCache;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@EligibilityCheck(targetClass = ActiveDriver.class, contextAware = true)
public class DriverTypeEligibilityCheck extends BaseEligibilityCheckItem<ActiveDriver> {

  static final String MESSAGE = "Driver not eligible to drive ";

  @Inject
  private DriverTypeCache driverTypeCache;
  @Inject
  private DriverTypeService driverTypeService;

  public DriverTypeEligibilityCheck(Map<String, Object> context) {
    super(context);
  }

  @Override
  public Optional<EligibilityCheckError> check(ActiveDriver subject) {
    boolean eligibleByDriverTypes = true;
    Set<String> carCategory = (Set<String>) context.get(DriverEligibilityCheckContext.CAR_CATEGORIES);
    Set<String> driverTypes = Optional.ofNullable((Set<String>) context.get(DriverEligibilityCheckContext.DRIVER_TYPES)).orElse(Collections.emptySet());
    Long cityId = (Long) context.get(DriverEligibilityCheckContext.CITY);
    if (CollectionUtils.isNotEmpty(driverTypes)) {
      Integer driverGrantedSubtypeBitmask = subject.getDriver().getGrantedDriverTypesBitmask();
      eligibleByDriverTypes = driverGrantedSubtypeBitmask != null
        && driverTypeCache.fromBitMask(driverGrantedSubtypeBitmask).containsAll(driverTypes)
        && driverTypeService.checkIfDriverTypesSupportCarCategories(carCategory, driverTypes, cityId);
    }
    return eligibleByDriverTypes ? Optional.empty() : Optional.of(new EligibilityCheckError(MESSAGE + carCategory + " " + driverTypes));
  }

  public void setDriverTypeCache(DriverTypeCache driverTypeCache) {
    this.driverTypeCache = driverTypeCache;
  }

  public void setDriverTypeService(DriverTypeService driverTypeService) {
    this.driverTypeService = driverTypeService;
  }
}
