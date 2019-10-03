package com.rideaustin.dispatch.error;

import javax.inject.Inject;

import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;

import com.rideaustin.service.model.Events;
import com.rideaustin.service.model.States;
import com.rideaustin.rest.exception.ServerError;
import com.rideaustin.service.GlobalExceptionEmailHelper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ErrorHandlingAction implements Action<States, Events> {

  @Inject
  private GlobalExceptionEmailHelper emailHelper;

  @Override
  public void execute(StateContext<States, Events> context) {
    emailHelper.processException(new ServerError("Something wrong with ride flow",
      new RideFlowException(context.getException(), context.getExtendedState())), null);
  }
}
