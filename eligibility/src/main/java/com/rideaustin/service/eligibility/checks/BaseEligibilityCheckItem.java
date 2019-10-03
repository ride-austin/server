package com.rideaustin.service.eligibility.checks;

import static com.rideaustin.service.eligibility.checks.EligibilityCheckItem.Order.ORDER_DOES_NOT_MATTER;

import java.util.Map;

public abstract class BaseEligibilityCheckItem<T> implements EligibilityCheckItem<T> {

  protected final Map<String, Object> context;

  public BaseEligibilityCheckItem(Map<String, Object> context) {
    this.context = context;
  }

  @Override
  public int getOrder() {
    return ORDER_DOES_NOT_MATTER;
  }
}
