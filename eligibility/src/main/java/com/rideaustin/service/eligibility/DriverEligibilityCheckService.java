package com.rideaustin.service.eligibility;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.stereotype.Service;

import com.rideaustin.clients.configuration.ConfigurationItemCache;
import com.rideaustin.filter.ClientType;
import com.rideaustin.model.ConfigurationItem;
import com.rideaustin.model.ride.ActiveDriver;
import com.rideaustin.model.ride.Car;
import com.rideaustin.model.user.Driver;
import com.rideaustin.rest.exception.BadRequestException;
import com.rideaustin.rest.exception.ServerError;
import com.rideaustin.service.CurrentUserService;
import com.rideaustin.service.eligibility.checks.EligibilityCheckItem;

@Service
public class DriverEligibilityCheckService extends BaseEligibilityCheckService<DriverEligibilityCheckContext> {

  static final String DRIVER_ELIGIBILITY = "driverEligibility";

  public DriverEligibilityCheckService(ConfigurationItemCache configurationCache, BeanFactory beanFactory, CurrentUserService currentUserService) {
    super(configurationCache, beanFactory, currentUserService);
  }

  @Override
  protected void doCheck(DriverEligibilityCheckContext context, Set<Class<?>> checks, Set<String> classNames, Map<String, Object> contextParams) throws BadRequestException {
    Set<EligibilityCheckItem<Driver>> driverChecks = getCheckBeans(getChecksFor(checks, classNames, Driver.class), contextParams);
    Set<EligibilityCheckItem<Car>> carChecks = getCheckBeans(getChecksFor(checks, classNames, Car.class), contextParams);
    Set<EligibilityCheckItem<ActiveDriver>> activeDriverChecks = getCheckBeans(getChecksFor(checks, classNames, ActiveDriver.class), contextParams);

    performCheck(driverChecks, context.getDriver());
    performCheck(carChecks, context.getCar());
    performCheck(activeDriverChecks, context.getActiveDriver());
  }

  @Override
  protected Set<String> getDefaultChecks(Long cityId) throws ServerError {
    ConfigurationItem eligibilityConfig = configurationCache.getConfigurationForClient(ClientType.DRIVER)
      .stream()
      .filter(ci -> cityId.equals(ci.getCityId()) && ci.getConfigurationKey().equals(DRIVER_ELIGIBILITY))
      .findFirst()
      .orElseThrow(() -> new ServerError("Can't find eligibility checks for city " + cityId));
    return new HashSet<>((Collection<? extends String>) ((Map) eligibilityConfig.getConfigurationObject()).get(DRIVER_ELIGIBILITY));
  }
}
