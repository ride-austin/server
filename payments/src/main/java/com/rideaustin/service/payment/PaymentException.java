package com.rideaustin.service.payment;

public class PaymentException extends Exception {

  public PaymentException(String message) {
    super(message);
  }

  public PaymentException(String message, Throwable cause) {
    super(message, cause);
  }
}
