package com.rideaustin.dispatch.actions;

import java.util.EnumSet;

import javax.inject.Inject;

import org.springframework.statemachine.StateContext;

import com.rideaustin.dispatch.aop.DeferredResultAction;
import com.rideaustin.dispatch.service.ConsecutiveDeclineUpdateServiceFactory;
import com.rideaustin.model.enums.RideStatus;
import com.rideaustin.model.ride.Ride;
import com.rideaustin.repo.dsl.RideDriverDispatchDslRepository;
import com.rideaustin.service.model.DispatchRequest;
import com.rideaustin.service.model.Events;
import com.rideaustin.service.model.States;
import com.rideaustin.service.model.context.DispatchContext;
import com.rideaustin.utils.dispatch.StateMachineUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RiderCancelAction extends BaseCancelAction {

  @Inject
  private RideDriverDispatchDslRepository rideDriverDispatchDslRepository;
  @Inject
  private ConsecutiveDeclineUpdateServiceFactory consecutiveDeclineUpdateServiceFactory;

  private final ReleaseRequestedDriversAction releaseRequestedDriversAction;

  public RiderCancelAction(ReleaseRequestedDriversAction releaseRequestedDriversAction) {
    this.releaseRequestedDriversAction = releaseRequestedDriversAction;
  }

  @Override
  protected void notifyRider(long id, StateContext<States, Events> context) {
    //do nothing
  }

  @Override
  @DeferredResultAction
  public void execute(StateContext<States, Events> context) {
    super.execute(context);
    if (EnumSet.of(States.DISPATCH_PENDING, States.REQUESTED).contains(context.getSource().getId())) {
      DispatchContext dispatchContext = StateMachineUtils.getDispatchContext(context);
      if (dispatchContext != null && dispatchContext.getCandidate() != null) {
        DispatchRequest dispatchRecord = rideDriverDispatchDslRepository.findLastMissedRequestByRide(dispatchContext.getId());
        if (dispatchRecord != null) {
          Ride ride = rideDslRepository.findOne(dispatchContext.getId());
          consecutiveDeclineUpdateServiceFactory.createService(dispatchContext.getDispatchType()).processDriverDecline(ride, dispatchRecord);
        }
      }
      releaseRequestedDriversAction.execute(context);
    }
  }

  @Override
  protected RideStatus getStatus() {
    return RideStatus.RIDER_CANCELLED;
  }

  @Override
  protected boolean isInSingleRide(long activeDriverId) {
    return rideDslRepository.findPrecedingRide(activeDriverId) == null;
  }
}
