package com.rideaustin.rest.exception;

import java.util.Random;

public class ServerError extends RideAustinException {  

  private static final Random RANDOM = new Random();
 
  private final String uniqueIdentifier;

  public ServerError(Exception e) {
    this(e, getRandomId());
  }

  public ServerError(String message) {
    this(message, null);
  }

  public ServerError(String message, Throwable cause) {
    super(message, cause);
    this.uniqueIdentifier = getRandomId();
  }

  public ServerError(Exception e, String id) {
    super(String.format("Something went wrong on server, Error code is %s Message: %s", id, e.getMessage()), e);
    this.uniqueIdentifier = id;
  }

  public String getUniqueIdentifier() {
    return uniqueIdentifier;
  }

  private static String getRandomId() {
    return Long.toHexString(RANDOM.nextLong());
  }
}
