package com.rideaustin.rest.exception;

public class ForbiddenException extends RideAustinException {

  public ForbiddenException() {
    this("Forbidden");
  }

  public ForbiddenException(String message) {
    super(message);
  }
}
