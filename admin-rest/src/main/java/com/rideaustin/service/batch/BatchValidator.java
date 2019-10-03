package com.rideaustin.service.batch;

import java.util.function.Predicate;

public interface BatchValidator {

  static Predicate<String> notEmpty() {
    Predicate<String> predicate = String::isEmpty;
    return predicate.negate();
  }
}
