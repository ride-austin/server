package com.rideaustin.dispatch.actions;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

import javax.inject.Inject;

import org.springframework.context.annotation.Lazy;
import org.springframework.statemachine.StateContext;

import com.rideaustin.dispatch.LogUtil;
import com.rideaustin.model.enums.DispatchStatus;
import com.rideaustin.model.ride.Ride;
import com.rideaustin.model.ride.RideDriverDispatch;
import com.rideaustin.repo.dsl.ActiveDriverDslRepository;
import com.rideaustin.repo.dsl.RideDriverDispatchDslRepository;
import com.rideaustin.repo.dsl.RideDslRepository;
import com.rideaustin.service.config.RideDispatchServiceConfig;
import com.rideaustin.service.event.EventsNotificationService;
import com.rideaustin.service.model.DispatchCandidate;
import com.rideaustin.service.model.Events;
import com.rideaustin.service.model.States;
import com.rideaustin.service.model.context.DispatchContext;
import com.rideaustin.utils.dispatch.StateMachineUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SendDispatchRequestAction extends AbstractContextPersistingAction {

  @Inject
  @Lazy
  private RideDslRepository rideDslRepository;
  @Inject
  @Lazy
  private RideDriverDispatchDslRepository rideDriverDispatchDslRepository;
  @Inject
  @Lazy
  private ActiveDriverDslRepository activeDriverDslRepository;
  @Inject
  @Lazy
  private EventsNotificationService eventsNotificationService;
  @Inject
  @Lazy
  private RideDispatchServiceConfig config;

  @Override
  public void execute(StateContext<States, Events> context) {
    DispatchContext dispatchContext = StateMachineUtils.getDispatchContext(context);
    DispatchCandidate candidate = dispatchContext.getCandidate();
    candidate.setRequestedAt(new Date());
    LogUtil.dispatchInfo(log, dispatchContext.getId(), String.format("Requested driver at %s", candidate.getRequestedAt()));
    StateMachineUtils.updateDispatchContext(context, dispatchContext, persister, environment);
    Ride ride = rideDslRepository.findOne(dispatchContext.getId());
    eventsNotificationService.sendRideRequest(ride, candidate, config.getRideRequestDeliveryTimeout() * 1000L,
      Date.from(Instant.now().plus(config.getDispatchAcceptanceTimeout(dispatchContext.getCityId()), ChronoUnit.SECONDS)),
      Date.from(Instant.now().plus(config.getDispatchAllowanceTimeout(dispatchContext.getCityId()), ChronoUnit.SECONDS)),
      candidate.getDrivingTimeToRider());

    // Track the dispatch
    RideDriverDispatch rdd = new RideDriverDispatch();
    rdd.setActiveDriver(activeDriverDslRepository.findById(candidate.getId()));
    rdd.setRide(ride);
    updateDispatchRecord(rdd, candidate);
    rideDriverDispatchDslRepository.save(rdd);
  }

  private void updateDispatchRecord(RideDriverDispatch rdd, DispatchCandidate candidate) {
    rdd.setDispatchedOn(new Date());
    rdd.setStatus(DispatchStatus.DISPATCHED);
    rdd.setDrivingTimeToRider(candidate.getDrivingTimeToRider());
    rdd.setDrivingDistanceToRider(candidate.getDrivingDistanceToRider());
    rdd.setDispatchLocationLat(candidate.getLatitude());
    rdd.setDispatchLocationLong(candidate.getLongitude());
  }
}
