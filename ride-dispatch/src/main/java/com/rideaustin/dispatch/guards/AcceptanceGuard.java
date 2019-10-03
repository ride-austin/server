package com.rideaustin.dispatch.guards;

import java.util.Optional;

import javax.inject.Inject;

import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.guard.Guard;

import com.rideaustin.service.model.Events;
import com.rideaustin.service.model.States;
import com.rideaustin.service.model.context.DispatchContext;
import com.rideaustin.service.RequestedDriversRegistry;
import com.rideaustin.utils.dispatch.StateMachineUtils;
import com.rideaustin.model.enums.DispatchStatus;
import com.rideaustin.model.ride.RideDriverDispatch;
import com.rideaustin.repo.dsl.RideDriverDispatchDslRepository;
import com.rideaustin.service.model.DispatchCandidate;

public class AcceptanceGuard implements Guard<States, Events> {

  @Inject
  private RequestedDriversRegistry requestedDriversRegistry;
  @Inject
  private RideDriverDispatchDslRepository rideDriverDispatchDslRepository;

  @Override
  public boolean evaluate(StateContext<States, Events> context) {
    DispatchContext dispatchContext = StateMachineUtils.getDispatchContext(context);
    boolean isRequested = Optional.ofNullable(dispatchContext.getCandidate())
      .map(DispatchCandidate::getId)
      .map(requestedDriversRegistry::isRequested)
      .orElse(false);
    boolean isDispatched = false;
    if (isRequested) {
      RideDriverDispatch rideDriverDispatch = rideDriverDispatchDslRepository.findByRideAndActiveDriverAndStatus(dispatchContext.getId(),
        DispatchStatus.DISPATCHED, dispatchContext.getCandidate().getId());
      isDispatched = rideDriverDispatch != null;
    }
    return isRequested && isDispatched;
  }
}
