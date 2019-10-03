package com.rideaustin.dispatch.actions;

import static com.rideaustin.dispatch.LogUtil.flowInfo;

import java.util.Date;
import java.util.Optional;

import javax.inject.Inject;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.statemachine.StateContext;

import com.rideaustin.dispatch.aop.DeferredResultAction;
import com.rideaustin.dispatch.messages.RideAcceptMessage;
import com.rideaustin.events.RideAcceptedEvent;
import com.rideaustin.model.Session;
import com.rideaustin.model.ride.ActiveDriver;
import com.rideaustin.repo.dsl.ActiveDriverDslRepository;
import com.rideaustin.repo.dsl.RideDslRepository;
import com.rideaustin.service.ActiveDriverLocationService;
import com.rideaustin.service.CurrentSessionService;
import com.rideaustin.service.RequestedDriversRegistry;
import com.rideaustin.service.model.DispatchCandidate;
import com.rideaustin.service.model.Events;
import com.rideaustin.service.model.States;
import com.rideaustin.service.model.context.DispatchContext;
import com.rideaustin.service.model.context.RideFlowContext;
import com.rideaustin.service.model.context.RideRequestContext;
import com.rideaustin.utils.dispatch.StateMachineUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AcceptRideAction extends AbstractContextPersistingAction {

  @Inject
  private CurrentSessionService currentSessionService;
  @Inject
  private ActiveDriverDslRepository activeDriverDslRepository;
  @Inject
  private RequestedDriversRegistry requestedDriversRegistry;
  @Inject
  private RideDslRepository rideDslRepository;
  @Inject
  private ApplicationEventPublisher publisher;
  @Inject
  private ActiveDriverLocationService activeDriverLocationService;

  @Override
  @DeferredResultAction
  public void execute(StateContext<States, Events> context) {
    DispatchContext dispatchContext = StateMachineUtils.getDispatchContext(context);
    RideRequestContext requestContext = StateMachineUtils.getRequestContext(context);
    DispatchCandidate candidate = dispatchContext.getCandidate();

    Long userId = new RideAcceptMessage(context.getMessageHeaders()).getUserId();

    ActiveDriver activeDriver = activeDriverDslRepository.findById(candidate.getId());

    updateFlowContext(context, candidate, userId);

    updateRideStatus(dispatchContext, activeDriver);

    updateActiveDriver(dispatchContext, requestContext);

    updateDispatchContext(context, dispatchContext);
  }

  private void updateDispatchContext(StateContext<States, Events> context, DispatchContext dispatchContext) {
    dispatchContext.setAccepted(true);
    StateMachineUtils.updateDispatchContext(context, dispatchContext, persister, environment);
  }

  private void updateActiveDriver(DispatchContext dispatchContext, RideRequestContext requestContext) {
    long activeDriverId = dispatchContext.getCandidate().getId();
    activeDriverDslRepository.setAvailableDriverAsRiding(activeDriverId);
    activeDriverLocationService.setActiveDriverAsRiding(activeDriverId, requestContext.getRequestedCarTypeCategory());
    requestedDriversRegistry.remove(activeDriverId);
    flowInfo(log, dispatchContext.getId(), String.format("Driver %d set as RIDING", activeDriverId));
  }

  private void updateRideStatus(DispatchContext dispatchContext, ActiveDriver activeDriver) {
    rideDslRepository.acceptRide(dispatchContext.getId(), activeDriver);
    flowInfo(log, dispatchContext.getId(), String.format("Driver %d accepted ride", dispatchContext.getCandidate().getId()));
    publisher.publishEvent(new RideAcceptedEvent(dispatchContext.getId()));
  }

  private void updateFlowContext(StateContext<States, Events> context, DispatchCandidate candidate, Long userId) {
    RideFlowContext flowContext = StateMachineUtils.getFlowContext(context);
    Optional<Session> driverSession = Optional.ofNullable(userId).map(currentSessionService::getCurrentSession);

    flowContext.setDriver(candidate.getId());
    flowContext.setDriverSession(driverSession.map(Session::getId).orElse(null));
    flowContext.setAcceptedOn(new Date());
    flowContext.setStacked(candidate.isStacked());
    StateMachineUtils.updateFlowContext(context, flowContext, persister, environment);
  }
}
