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
import com.rideaustin.model.user.Rider;
import com.rideaustin.rest.exception.BadRequestException;
import com.rideaustin.rest.exception.ServerError;
import com.rideaustin.service.CurrentUserService;
import com.rideaustin.service.eligibility.checks.EligibilityCheckItem;

@Service
public class RiderEligibilityCheckService extends BaseEligibilityCheckService<RiderEligibilityCheckContext> {

  private static final String RIDER_ELIGIBILITY = "riderEligibility";

  public RiderEligibilityCheckService(ConfigurationItemCache configurationCache, BeanFactory beanFactory, CurrentUserService currentUserService) {
    super(configurationCache, beanFactory, currentUserService);
  }

  @Override
  protected void doCheck(RiderEligibilityCheckContext context, Set<Class<?>> checks, Set<String> classNames, Map<String, Object> contextParams) throws BadRequestException {
    Set<EligibilityCheckItem<Rider>> riderChecks = getCheckBeans(getChecksFor(checks, classNames, Rider.class), contextParams);

    performCheck(riderChecks, context.getRider());
  }

  @Override
  protected Set<String> getDefaultChecks(Long cityId) throws ServerError {
    ConfigurationItem eligibilityConfig = configurationCache.getConfigurationForClient(ClientType.RIDER)
      .stream()
      .filter(ci -> cityId.equals(ci.getCityId()) && ci.getConfigurationKey().equals(RIDER_ELIGIBILITY))
      .findFirst()
      .orElseThrow(() -> new ServerError("Can't find eligibility checks for city " + cityId));
    return new HashSet<>((Collection<? extends String>) ((Map) eligibilityConfig.getConfigurationObject()).get(RIDER_ELIGIBILITY));
  }
}
