package com.rideaustin.service.thirdparty;

import com.rideaustin.rest.exception.ServerError;
import com.twilio.exception.TwilioException;

public class TwilioCallException extends ServerError {

  public TwilioCallException(TwilioException e) {
    super(e);
  }

  @Override
  public String getMessage() {
    return "Failed to initiate call";
  }
}
