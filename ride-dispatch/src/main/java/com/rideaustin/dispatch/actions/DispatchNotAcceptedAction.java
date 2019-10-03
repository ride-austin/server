package com.rideaustin.dispatch.actions;

import static com.rideaustin.dispatch.LogUtil.dispatchInfo;

import javax.inject.Inject;

import org.springframework.statemachine.StateContext;

import com.rideaustin.dispatch.service.ConsecutiveDeclineUpdateServiceFactory;
import com.rideaustin.model.enums.DispatchStatus;
import com.rideaustin.model.ride.Ride;
import com.rideaustin.repo.dsl.RideDriverDispatchDslRepository;
import com.rideaustin.repo.dsl.RideDslRepository;
import com.rideaustin.service.model.DispatchRequest;
import com.rideaustin.service.model.Events;
import com.rideaustin.service.model.States;
import com.rideaustin.service.model.context.DispatchContext;
import com.rideaustin.utils.dispatch.StateMachineUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DispatchNotAcceptedAction extends ReleaseRequestedDriversAction {

  @Inject
  private RideDslRepository rideDslRepository;
  @Inject
  private RideDriverDispatchDslRepository rideDriverDispatchDslRepository;
  @Inject
  private ConsecutiveDeclineUpdateServiceFactory declineUpdateServiceFactory;

  @Override
  public void execute(StateContext<States, Events> context) {
    DispatchContext dispatchContext = StateMachineUtils.getDispatchContext(context);
    if (dispatchContext != null && dispatchContext.getCandidate() != null) {
      dispatchInfo(log, dispatchContext.getId(), "Declining missed request");
      rideDriverDispatchDslRepository.declineRequest(dispatchContext.getId(), dispatchContext.getCandidate().getId(), DispatchStatus.MISSED);
      Ride ride = rideDslRepository.findOne(dispatchContext.getId());
      DispatchRequest lastDeclinedDispatch = rideDriverDispatchDslRepository.findLastMissedRequestByRide(dispatchContext.getId());
      declineUpdateServiceFactory.createService(dispatchContext.getDispatchType()).processDriverDecline(ride, lastDeclinedDispatch);
    }
    super.execute(context);
  }
}
