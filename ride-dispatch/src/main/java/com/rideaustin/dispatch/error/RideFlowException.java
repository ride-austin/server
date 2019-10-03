package com.rideaustin.dispatch.error;

import org.springframework.statemachine.ExtendedState;

public class RideFlowException extends RuntimeException {

  public RideFlowException(Throwable e, ExtendedState extendedState) {
    super(String.format("Message: %s%n%nContext: %s", e.getMessage(), extendedState), e);
  }
}
