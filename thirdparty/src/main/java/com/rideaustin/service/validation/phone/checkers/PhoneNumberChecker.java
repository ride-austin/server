package com.rideaustin.service.validation.phone.checkers;

import java.util.Optional;

import com.rideaustin.service.thirdparty.lookup.PhoneNumberInfo;

@FunctionalInterface
public interface PhoneNumberChecker {
  Optional<CheckerError> check(PhoneNumberInfo info);

  class CheckerError {
    private final String message;

    public CheckerError(String message) {
      this.message = message;
    }

    public String getMessage() {
      return message;
    }
  }
}
