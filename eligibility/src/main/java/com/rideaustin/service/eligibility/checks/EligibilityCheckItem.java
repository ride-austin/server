package com.rideaustin.service.eligibility.checks;

import java.util.Optional;

import com.rideaustin.service.eligibility.EligibilityCheckError;

public interface EligibilityCheckItem<T> {

  Optional<EligibilityCheckError> check(T subject);

  int getOrder();

  class Order {
    public static final int ORDER_DOES_NOT_MATTER = 100;
    public static final int FIRST_ORDER = 1;

    private Order() {
    }
  }

}
