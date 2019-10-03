package com.rideaustin.service.thirdparty;


public class SMSException extends Exception {

  public SMSException(Throwable cause) {
    super(cause);
  }

  public SMSException(String message) {
    super(message);
  }

}
