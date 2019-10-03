package com.rideaustin.service.thirdparty.lookup;

import com.rideaustin.rest.exception.ServerError;

public class PhoneNumberLookupException extends ServerError {

  private static final String DEFAULT_MESSAGE = "Error occurred during phone number lookup";

  public PhoneNumberLookupException() {
    super(DEFAULT_MESSAGE);
  }

  public PhoneNumberLookupException(String message) {
    super(message);
  }

}
