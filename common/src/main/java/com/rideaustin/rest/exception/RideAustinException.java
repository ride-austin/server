package com.rideaustin.rest.exception;

public abstract class RideAustinException extends Exception {

  public RideAustinException(String message) {
    super(message);
  }

  public RideAustinException(String message, Throwable cause) {
    super(message, cause);
  }
}
