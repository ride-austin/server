package com.rideaustin.rest.exception;

public class UnAuthorizedException extends RideAustinException {

  public UnAuthorizedException() {
    this("Not Authorized");
  }

  public UnAuthorizedException(String message) {
    super(message);
  }
}
