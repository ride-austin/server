package com.rideaustin.dispatch.actions;

import static com.rideaustin.dispatch.LogUtil.dispatchInfo;

import java.util.List;

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
public class DispatchDeclineAction extends ReleaseRequestedDriversAction {

  @Inject
  private RideDslRepository rideDslRepository;
  @Inject
  private RideDriverDispatchDslRepository rideDriverDispatchDslRepository;
  @Inject
  private ConsecutiveDeclineUpdateServiceFactory consecutiveDeclineUpdateServiceFactory;

  @Override
  public void execute(StateContext<States, Events> context) {
    dispatchInfo(log, StateMachineUtils.getRequestContext(context), "Executing decline action");
    DispatchContext dispatchContext = StateMachineUtils.getDispatchContext(context);

    Ride ride = rideDslRepository.findOne(StateMachineUtils.getRideId(context));
    List<DispatchRequest> dispatchedRequests = rideDriverDispatchDslRepository.findDispatchedRequests(ride);
    for (DispatchRequest request : dispatchedRequests) {
      consecutiveDeclineUpdateServiceFactory.createService(dispatchContext.getDispatchType()).processDriverDecline(ride, request);
    }

    rideDriverDispatchDslRepository.declineRequest(dispatchContext.getId(), dispatchContext.getCandidate().getId(), DispatchStatus.DECLINED);

    super.execute(context);
  }

}
