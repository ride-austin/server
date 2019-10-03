package com.rideaustin.service.eligibility;

public class EligibilityCheckError {

  private final String message;

  public EligibilityCheckError(String message) {
    this.message = message;
  }

  public String getMessage() {
    return message;
  }
}
