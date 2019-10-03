package com.rideaustin.dispatch.actions;

import java.util.Date;

import javax.inject.Inject;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.statemachine.StateContext;

import com.rideaustin.dispatch.aop.DeferredResultAction;
import com.rideaustin.dispatch.messages.DriverReachMessage;
import com.rideaustin.events.DriverReachedEvent;
import com.rideaustin.model.enums.RideStatus;
import com.rideaustin.repo.dsl.RideDslRepository;
import com.rideaustin.service.model.Events;
import com.rideaustin.service.model.States;
import com.rideaustin.service.model.context.RideFlowContext;
import com.rideaustin.service.notification.RideFlowPushNotificationFacade;
import com.rideaustin.utils.dispatch.StateMachineUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DriverReachAction extends AbstractContextPersistingAction {

  @Inject
  private RideFlowPushNotificationFacade pushNotificationsFacade;
  @Inject
  private ApplicationEventPublisher publisher;
  @Inject
  private RideDslRepository rideDslRepository;

  @Override
  @DeferredResultAction
  public void execute(StateContext<States, Events> context) {
    Long rideId = StateMachineUtils.getRideId(context);
    RideFlowContext flowContext = StateMachineUtils.getFlowContext(context);
    Date reachedDate = new DriverReachMessage(context.getMessageHeaders()).getReachedDate();
    flowContext.setReachedOn(reachedDate);
    StateMachineUtils.updateFlowContext(context, flowContext, persister, environment);

    // Notify the rider
    if (rideId != null) {
      rideDslRepository.setStatus(rideId, RideStatus.DRIVER_REACHED);
      pushNotificationsFacade.sendRideUpdateToRider(rideId);
      publisher.publishEvent(new DriverReachedEvent(this, rideId));
    }
  }
}
