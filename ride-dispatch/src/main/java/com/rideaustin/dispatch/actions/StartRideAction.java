package com.rideaustin.dispatch.actions;

import static com.rideaustin.dispatch.LogUtil.dispatchInfo;
import static com.rideaustin.dispatch.LogUtil.flowInfo;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.statemachine.StateContext;

import com.rideaustin.dispatch.aop.DeferredResultAction;
import com.rideaustin.dispatch.messages.RideStartMessage;
import com.rideaustin.dispatch.tasks.UpfrontChargeTask;
import com.rideaustin.model.enums.RideStatus;
import com.rideaustin.model.ride.RideTracker;
import com.rideaustin.repo.dsl.RideDslRepository;
import com.rideaustin.service.RideTrackerService;
import com.rideaustin.service.RiderLocationService;
import com.rideaustin.service.config.RidePaymentConfig;
import com.rideaustin.service.model.Events;
import com.rideaustin.service.model.States;
import com.rideaustin.service.model.context.RideFlowContext;
import com.rideaustin.service.model.context.RideRequestContext;
import com.rideaustin.service.notification.RideFlowPushNotificationFacade;
import com.rideaustin.utils.dispatch.StateMachineUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class StartRideAction extends AbstractContextPersistingAction {

  @Inject
  private RideTrackerService rideTrackerService;
  @Inject
  private RideFlowPushNotificationFacade pushNotificationFacade;
  @Inject
  private RiderLocationService riderLocationService;
  @Inject
  private RideDslRepository rideDslRepository;
  @Inject
  private RidePaymentConfig ridePaymentConfig;
  @Inject
  @Named("taskExecutor")
  private TaskScheduler scheduler;
  @Inject
  private BeanFactory beanFactory;

  @Override
  @DeferredResultAction
  public void execute(StateContext<States, Events> context) {
    Long rideId = StateMachineUtils.getRideId(context);
    flowInfo(log, rideId, "Starting ride");
    RideRequestContext requestContext = StateMachineUtils.getRequestContext(context);
    RideFlowContext flowContext = StateMachineUtils.getFlowContext(context);
    Date startDate = new RideStartMessage(context.getMessageHeaders()).getStartDate();

    flowContext.setStartedOn(startDate);
    flowContext.setStacked(false);
    StateMachineUtils.updateFlowContext(context, flowContext, persister, environment);

    RideTracker rideTracker = new RideTracker(requestContext.getStartLocationLat(), requestContext.getStartLocationLong(),
      null, null, null, 0L);
    rideTrackerService.updateRideTracker(rideId, rideTracker, startDate);
    riderLocationService.evictRiderLocation(requestContext.getRiderId());
    // notify the rider
    if (rideId != null) {
      pushNotificationFacade.sendRideUpdateToRider(rideId, RideStatus.ACTIVE);
      rideDslRepository.setStatus(rideId, RideStatus.ACTIVE);
    }

    if (ridePaymentConfig.isUpfrontPricingEnabled()) {
      dispatchInfo(log, requestContext, "Scheduling upfront charge");
      scheduler.schedule(beanFactory.getBean(UpfrontChargeTask.class)
        .withRideId(rideId), Date.from(Instant.now().plus(ridePaymentConfig.getUpfrontPricingTimeout(), ChronoUnit.SECONDS)));
    }
  }
}
