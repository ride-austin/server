package com.rideaustin.rest.exception;

public class TermsNotAcceptedException extends RideAustinException {

  public TermsNotAcceptedException() {
    this("Terms not accepted");
  }

  public TermsNotAcceptedException(String message) {
    super(message);
  }
}
