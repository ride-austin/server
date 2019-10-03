package com.rideaustin.service.eligibility;

import java.util.Map;
import java.util.Set;

public interface EligibilityCheckContext {
  Map<String, Object> getParams();

  Set<Class<?>> getRequestedChecks();
}
