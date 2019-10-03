package com.rideaustin.dispatch.actions;

import static com.rideaustin.dispatch.LogUtil.flowError;
import static com.rideaustin.dispatch.LogUtil.flowInfo;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;

import com.rideaustin.dispatch.KillInceptionMachineMessage;
import com.rideaustin.dispatch.error.RideFlowException;
import com.rideaustin.dispatch.service.ConsecutiveDeclineUpdateServiceFactory;
import com.rideaustin.model.enums.RideStatus;
import com.rideaustin.model.ride.Ride;
import com.rideaustin.repo.dsl.ActiveDriverDslRepository;
import com.rideaustin.repo.dsl.RideDriverDispatchDslRepository;
import com.rideaustin.repo.dsl.RideDslRepository;
import com.rideaustin.service.model.DispatchCandidate;
import com.rideaustin.service.model.DispatchRequest;
import com.rideaustin.service.model.Events;
import com.rideaustin.service.model.States;
import com.rideaustin.service.model.context.DispatchContext;
import com.rideaustin.service.model.context.RideRequestContext;
import com.rideaustin.service.notification.RideFlowPushNotificationFacade;
import com.rideaustin.service.thirdparty.StripeService;
import com.rideaustin.utils.dispatch.StateMachineUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NoAvailableDriverAction implements Action<States, Events> {

  @Inject
  private RideDslRepository rideDslRepository;
  @Inject
  private RideDriverDispatchDslRepository rideDriverDispatchDslRepository;
  @Inject
  private ActiveDriverDslRepository activeDriverDslRepository;
  @Inject
  private RideFlowPushNotificationFacade pushNotificationsFacade;
  @Inject
  private StripeService stripeService;
  @Inject
  private ConsecutiveDeclineUpdateServiceFactory consecutiveDeclineUpdateServiceFactory;
  @Inject
  @Named("inceptionMachinesTopic")
  private ChannelTopic inceptionMachinesTopic;
  @Inject
  @Named("rideFlowRedisTemplate")
  private RedisTemplate redisTemplate;

  @Override
  public void execute(StateContext<States, Events> context) {
    RideRequestContext requestContext = StateMachineUtils.getRequestContext(context);
    flowInfo(log, requestContext, "NoAvailableDriverAction started");

    Long rideId = StateMachineUtils.getRideId(context);
    if (rideId == null) {
      return;
    }
    try {
      Ride ride = rideDslRepository.findOne(rideId);

      // Mark driver statuses correctly
      declineCurrentlyDispatchedDrivers(ride, StateMachineUtils.getDispatchContext(context));

      stripeService.refundPreCharge(ride);
      ride.setStatus(RideStatus.NO_AVAILABLE_DRIVER);

      rideDslRepository.save(ride);

      // Notify the rider that no drivers are available
      pushNotificationsFacade.sendRideUpdateToRider(rideId);
      flowInfo(log, requestContext, String.format("No drivers available for ride #%d", ride.getId()));

      Optional<DispatchCandidate> candidate = Optional.ofNullable(StateMachineUtils.getDispatchContext(context)).map(DispatchContext::getCandidate);
      candidate.ifPresent(c -> activeDriverDslRepository.setRidingDriverAsAvailable(c.getId()));
      redisTemplate.convertAndSend(inceptionMachinesTopic.getTopic(), new KillInceptionMachineMessage(rideId));
    } catch (Exception e) {
      flowError(log, requestContext, String.format("Failed to set ride %d to NO_AVAILABLE_DRIVER", rideId), e);
      throw new RideFlowException(e, context.getExtendedState());
    }
  }

  private void declineCurrentlyDispatchedDrivers(Ride ride, DispatchContext dispatchContext) {
    // Mark all other dispatched drivers for this ride as DECLINED
    List<DispatchRequest> dispatchedRequests = rideDriverDispatchDslRepository.findDispatchedRequests(ride);
    for (DispatchRequest request : dispatchedRequests) {
      consecutiveDeclineUpdateServiceFactory.createService(dispatchContext.getDispatchType()).processDriverDecline(ride, request);
    }
    rideDriverDispatchDslRepository.missRequests(dispatchedRequests.stream().map(DispatchRequest::getId).collect(Collectors.toList()));
  }

}
