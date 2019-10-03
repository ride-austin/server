package com.rideaustin.dispatch.actions;

import static com.rideaustin.dispatch.LogUtil.dispatchInfo;

import javax.inject.Inject;

import org.springframework.context.annotation.Lazy;
import org.springframework.statemachine.StateContext;

import com.rideaustin.service.RequestedDriversRegistry;
import com.rideaustin.service.StackedDriverRegistry;
import com.rideaustin.service.model.Events;
import com.rideaustin.service.model.States;
import com.rideaustin.service.model.context.DispatchContext;
import com.rideaustin.service.model.context.RideRequestContext;
import com.rideaustin.utils.dispatch.StateMachineUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ReleaseRequestedDriversAction extends AbstractContextPersistingAction {

  @Inject
  protected StackedDriverRegistry stackedDriverRegistry;
  @Inject @Lazy
  private RequestedDriversRegistry requestedDriversRegistry;

  @Override
  public void execute(StateContext<States, Events> context) {
    RideRequestContext requestContext = StateMachineUtils.getRequestContext(context);
    dispatchInfo(log, requestContext,"Releasing requested drivers");
    DispatchContext dispatchContext = StateMachineUtils.getDispatchContext(context);
    if (dispatchContext != null && dispatchContext.getCandidate() != null) {
      long candidateId = dispatchContext.getCandidate().getId();
      if (requestedDriversRegistry.isRequested(candidateId)) {
        requestedDriversRegistry.remove(candidateId);
      }
      if (stackedDriverRegistry.isStacked(candidateId)) {
        stackedDriverRegistry.makeStackable(candidateId);
      }
      requestContext.getIgnoreIds().add(candidateId);
      StateMachineUtils.updateRequestContext(context, requestContext, persister, environment);
      dispatchContext.setCandidate(null);
      dispatchContext.setAccepted(false);
      StateMachineUtils.updateDispatchContext(context, dispatchContext, persister, environment);
    }
  }
}
