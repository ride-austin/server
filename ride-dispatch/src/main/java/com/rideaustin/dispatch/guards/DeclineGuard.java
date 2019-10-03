package com.rideaustin.dispatch.guards;

import static com.rideaustin.dispatch.LogUtil.dispatchInfo;

import java.util.Optional;

import javax.inject.Inject;

import org.springframework.core.env.Environment;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.guard.Guard;

import com.rideaustin.dispatch.messages.DeclineDispatchMessage;
import com.rideaustin.model.enums.DispatchStatus;
import com.rideaustin.model.ride.RideDriverDispatch;
import com.rideaustin.repo.dsl.RideDriverDispatchDslRepository;
import com.rideaustin.service.model.DispatchCandidate;
import com.rideaustin.service.model.Events;
import com.rideaustin.service.model.States;
import com.rideaustin.service.model.context.DispatchContext;
import com.rideaustin.service.model.context.RideRequestContext;
import com.rideaustin.utils.dispatch.StateMachineUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DeclineGuard implements Guard<States, Events> {

  @Inject
  private RideDriverDispatchDslRepository rideDriverDispatchDslRepository;
  @Inject
  private Environment environment;

  @Override
  public boolean evaluate(StateContext<States, Events> context) {
    RideRequestContext requestContext = StateMachineUtils.getRequestContext(context);
    dispatchInfo(log, requestContext, "Evaluating transition to declined");
    DispatchContext dispatchContext = StateMachineUtils.getDispatchContext(context);

    DeclineDispatchMessage message = new DeclineDispatchMessage(context.getMessageHeaders());
    RideDriverDispatch dispatch =
      rideDriverDispatchDslRepository.findByRideAndStatus(dispatchContext.getId(), DispatchStatus.DISPATCHED);
    boolean isDispatchedToDriver = dispatch != null && Optional.ofNullable(dispatchContext.getCandidate())
      .map(DispatchCandidate::getUserId)
      .map(id -> id.equals(message.getUserId()))
      .orElse(false);
    dispatchInfo(log, requestContext, String.format("Is request dispatched to driver: %s", isDispatchedToDriver));
    Boolean configured = environment.getProperty("dispatch.prepone_declined_driver_dispatches", Boolean.class, Boolean.TRUE);
    dispatchInfo(log, requestContext, String.format("Is search restarted immediately: %s", configured));
    return isDispatchedToDriver && configured;
  }
}
