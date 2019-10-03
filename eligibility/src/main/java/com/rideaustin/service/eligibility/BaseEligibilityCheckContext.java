package com.rideaustin.service.eligibility;

import java.util.Map;
import java.util.Set;

public abstract class BaseEligibilityCheckContext implements EligibilityCheckContext {

  private final Map<String, Object> params;
  private final Set<Class<?>> requestedChecks;

  protected BaseEligibilityCheckContext(Map<String, Object> params, Set<Class<?>> requestedChecks) {
    this.params = params;
    this.requestedChecks = requestedChecks;
  }

  @Override
  public Map<String, Object> getParams() {
    return params;
  }

  @Override
  public Set<Class<?>> getRequestedChecks() {
    return requestedChecks;
  }
}
