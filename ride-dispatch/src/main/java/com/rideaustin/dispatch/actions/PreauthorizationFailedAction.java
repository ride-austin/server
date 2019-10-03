package com.rideaustin.dispatch.actions;

import org.springframework.statemachine.StateContext;

import com.rideaustin.dispatch.LogUtil;
import com.rideaustin.service.model.Events;
import com.rideaustin.service.model.States;
import com.rideaustin.service.model.context.RideRequestContext;
import com.rideaustin.utils.dispatch.StateMachineUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PreauthorizationFailedAction extends NoAvailableDriverAction {

  private final ReleaseRequestedDriversAction releaseRequestedDriversAction;

  public PreauthorizationFailedAction(ReleaseRequestedDriversAction releaseRequestedDriversAction) {
    this.releaseRequestedDriversAction = releaseRequestedDriversAction;
  }

  @Override
  public void execute(StateContext<States, Events> context) {
    final RideRequestContext requestContext = StateMachineUtils.getRequestContext(context);
    LogUtil.dispatchInfo(log, requestContext, "Executing PreauthorizationFailedAction");
    LogUtil.dispatchInfo(log, requestContext, "Executing NADA");
    super.execute(context);
    LogUtil.dispatchInfo(log, requestContext, "Executing RRDA");
    releaseRequestedDriversAction.execute(context);
  }
}
