package com.rideaustin.dispatch.actions;

import static com.rideaustin.dispatch.LogUtil.flowError;
import static com.rideaustin.dispatch.LogUtil.flowInfo;

import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import org.redisson.api.RSemaphore;
import org.redisson.api.RedissonClient;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;

import com.rideaustin.dispatch.error.RideFlowException;
import com.rideaustin.model.enums.ActiveDriverStatus;
import com.rideaustin.model.enums.EventType;
import com.rideaustin.model.enums.RideStatus;
import com.rideaustin.model.ride.FareDetails;
import com.rideaustin.model.ride.Ride;
import com.rideaustin.model.ride.RideTracker;
import com.rideaustin.repo.dsl.ActiveDriverDslRepository;
import com.rideaustin.repo.dsl.RideDslRepository;
import com.rideaustin.rest.exception.RideAustinException;
import com.rideaustin.service.ActiveDriverLocationService;
import com.rideaustin.service.FareService;
import com.rideaustin.service.RequestedDriversRegistry;
import com.rideaustin.service.RideTrackerService;
import com.rideaustin.service.SchedulerService;
import com.rideaustin.service.StackedDriverRegistry;
import com.rideaustin.service.config.RidePaymentConfig;
import com.rideaustin.service.event.EventsNotificationService;
import com.rideaustin.service.model.DispatchCandidate;
import com.rideaustin.service.model.Events;
import com.rideaustin.service.model.OnlineDriverDto;
import com.rideaustin.service.model.States;
import com.rideaustin.service.model.context.DispatchContext;
import com.rideaustin.service.model.context.RideRequestContext;
import com.rideaustin.service.notification.RideFlowPushNotificationFacade;
import com.rideaustin.service.ride.jobs.ProcessRidePaymentJob;
import com.rideaustin.service.thirdparty.StripeService;
import com.rideaustin.utils.dispatch.StateMachineUtils;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class BaseCancelAction implements Action<States, Events> {

  @Inject
  protected RideFlowPushNotificationFacade pushNotificationsFacade;
  @Inject
  protected RideDslRepository rideDslRepository;
  @Inject
  protected ActiveDriverDslRepository activeDriverDslRepository;
  @Inject
  private RequestedDriversRegistry requestedDriversRegistry;
  @Inject
  protected StackedDriverRegistry stackedDriverRegistry;
  @Inject
  protected ActiveDriverLocationService activeDriverLocationService;

  @Inject
  protected StripeService stripeService;
  @Inject
  private RideTrackerService rideTrackerService;
  @Inject
  private FareService fareService;
  @Inject
  private SchedulerService schedulerService;
  @Inject
  private EventsNotificationService eventsNotificationService;
  @Inject
  private RedissonClient redissonClient;
  @Inject
  private RidePaymentConfig ridePaymentConfig;

  @Inject
  private DumpContextAction dumpContextAction;

  @Override
  public void execute(StateContext<States, Events> context) {
    Long rideId = StateMachineUtils.getRideId(context);
    RSemaphore semaphore = null;
    if (ridePaymentConfig.isAsyncPreauthEnabled()) {
      semaphore = redissonClient.getSemaphore(String.format("ride:%d:preauth", rideId));
    }
    RideRequestContext requestContext = StateMachineUtils.getRequestContext(context);
    try {
      Ride ride = null;
      flowInfo(log, requestContext, "CANCEL RIDE try to acquire semaphore");
      if (semaphore == null || acquired(semaphore)) {
        ride = rideDslRepository.findOneWithRider(rideId);
      }
      if (ride == null || ride.getStatus() == getStatus()) {
        flowInfo(log, requestContext, "CANCEL RIDE is not found or semaphore failed to acquire in 5 seconds");
        return;
      }

      makeRefund(ride);

      RideStatus sourceStatus = ride.getStatus();
      DispatchContext dispatchContext = StateMachineUtils.getDispatchContext(context);
      flowInfo(log, requestContext, String.format("Updating ride status to %s", getStatus()));
      ride.setStatus(getStatus());

      RideTracker rideTracker = new RideTracker(ride.getStartLocationLat(), ride.getStartLocationLong(),
        null, null, null, 0L);
      rideTrackerService.updateRideTracker(ride.getId(), rideTracker);

      rideTrackerService.saveStaticImage(ride);

      notifyRider(ride.getId(), context);

      Optional<FareDetails> fareDetails = Optional.empty();
      if (dispatchContext != null && dispatchContext.getCandidate() != null) {
        notifyDriver(ride, dispatchContext.getCandidate());

        long activeDriverId = dispatchContext.getCandidate().getId();
        boolean requested = requestedDriversRegistry.isRequested(activeDriverId);
        if (requested) {
          requestedDriversRegistry.remove(activeDriverId);
        }
        boolean shouldChargeCancellationFee = shouldChargeCancellationFee(ride, sourceStatus, activeDriverId);
        fareDetails = fareService.processCancellation(ride, shouldChargeCancellationFee);
        if (shouldChargeCancellationFee && !schedulerService.checkIfExists(Long.toString(rideId), "RidePayment")) {
          schedulerService.triggerJob(ProcessRidePaymentJob.class,
            Long.toString(rideId), "RidePayment", 10,
            Collections.singletonMap("rideId", rideId));
        }
        releaseActiveDriver(sourceStatus, activeDriverId);
      }
      rideDslRepository.cancelRide(rideId, getStatus(), fareDetails.orElse(ride.getFareDetails()), ride.getPaymentStatus());
    } catch (Exception e) {
      flowError(log, requestContext, String.format("Failed to cancel ride #%d", rideId), e);
      throw new RideFlowException(e, context.getExtendedState());
    } finally {
      if (semaphore != null) {
        semaphore.release(1);
      }
      dumpContextAction.execute(context);
    }
  }

  private boolean acquired(RSemaphore semaphore) {
    try {
      return semaphore.tryAcquire(5000, TimeUnit.MILLISECONDS);
    } catch (InterruptedException e) {
      log.error("Failed to acquire semaphore", e);
      Thread.currentThread().interrupt();
      return true;
    }
  }

  protected void makeRefund(Ride ride) throws RideAustinException {
    log.info(String.format("[Ride #%d] Refunding precharge", ride.getId()));
    stripeService.refundPreCharge(ride);
  }

  protected void releaseActiveDriver(RideStatus status, long activeDriverId) {
    if (status == RideStatus.DRIVER_ASSIGNED) {
      log.info(String.format("[CANCEL] AD %d: Releasing AD for DA ride", activeDriverId));
      if (isInSingleRide(activeDriverId)) {
        log.info(String.format("[CANCEL] AD %d: AD is in single ride, setting as available", activeDriverId));
        setRidingAsAvailable(activeDriverId);
      } else {
        log.info(String.format("[CANCEL] AD %d: AD is not in single ride, making stackable", activeDriverId));
        stackedDriverRegistry.makeStackable(activeDriverId);
      }
    } else if (status == RideStatus.DRIVER_REACHED) {
      log.info(String.format("[CANCEL] AD %d: Releasing AD for DR ride, setting as available", activeDriverId));
      setRidingAsAvailable(activeDriverId);
    }
  }

  protected void setRidingAsAvailable(long activeDriverId) {
    OnlineDriverDto onlineDriver = activeDriverLocationService.updateActiveDriverLocationStatus(activeDriverId, ActiveDriverStatus.AVAILABLE);
    log.info(String.format("[CANCEL] AD %d: Status after update is %s", activeDriverId, onlineDriver == null ? "[null]" : onlineDriver.getStatus()));
    activeDriverDslRepository.setRidingDriverAsAvailable(activeDriverId);
  }

  protected abstract boolean isInSingleRide(long activeDriverId);

  protected boolean shouldChargeCancellationFee(Ride ride, RideStatus sourceStatus, long activeDriverId) {
    boolean stacked = sourceStatus == RideStatus.DRIVER_ASSIGNED && !isInSingleRide(activeDriverId);
    return !stacked
      && ride.getChargeId() == null
      && fareService.shouldChargeCancellationFee(ride, getStatus());
  }

  protected abstract void notifyRider(long id, StateContext<States, Events> context);

  protected abstract RideStatus getStatus();

  private void notifyDriver(Ride ride, @NonNull DispatchCandidate candidate) {
    EventType eventType = EventType.from(getStatus());
    eventsNotificationService.sendRideUpdateToDriver(ride, candidate, eventType);
  }
}
