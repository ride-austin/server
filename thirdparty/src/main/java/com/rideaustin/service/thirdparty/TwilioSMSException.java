package com.rideaustin.service.thirdparty;

import com.rideaustin.rest.exception.ServerError;
import com.twilio.exception.TwilioException;

public class TwilioSMSException extends ServerError {
  public TwilioSMSException(TwilioException e) {
    super(e);
  }

  @Override
  public String getMessage() {
    return "Failed to send text message";
  }
}
