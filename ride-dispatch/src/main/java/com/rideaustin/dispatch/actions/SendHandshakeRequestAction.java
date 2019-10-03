package com.rideaustin.dispatch.actions;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

import javax.inject.Inject;

import org.springframework.statemachine.StateContext;

import com.rideaustin.service.config.RideDispatchServiceConfig;
import com.rideaustin.service.event.EventsNotificationService;
import com.rideaustin.service.model.DispatchCandidate;
import com.rideaustin.service.model.Events;
import com.rideaustin.service.model.States;
import com.rideaustin.service.model.context.DispatchContext;
import com.rideaustin.utils.dispatch.StateMachineUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SendHandshakeRequestAction extends AbstractContextPersistingAction {

  @Inject
  private EventsNotificationService eventsNotificationService;
  @Inject
  private RideDispatchServiceConfig config;

  @Override
  public void execute(StateContext<States, Events> context) {
    DispatchContext dispatchContext = StateMachineUtils.getDispatchContext(context);
    DispatchCandidate candidate = dispatchContext.getCandidate();
    eventsNotificationService.sendHandshakeRequest(dispatchContext.getId(), candidate, config.getRideRequestDeliveryTimeout() * 1000L,
      Date.from(Instant.now().plus(config.getDispatchAllowanceTimeout(dispatchContext.getCityId()), ChronoUnit.SECONDS)));
  }
}
