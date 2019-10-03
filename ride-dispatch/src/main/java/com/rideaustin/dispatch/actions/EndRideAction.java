package com.rideaustin.dispatch.actions;

import static com.rideaustin.dispatch.LogUtil.flowError;
import static com.rideaustin.dispatch.LogUtil.flowInfo;

import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.Optional;

import javax.inject.Inject;

import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.StateMachineContext;
import org.springframework.statemachine.StateMachinePersist;
import org.springframework.web.context.request.async.DeferredResult;

import com.rideaustin.dispatch.messages.EndRideMessage;
import com.rideaustin.model.Address;
import com.rideaustin.model.airports.Airport;
import com.rideaustin.model.enums.ActiveDriverStatus;
import com.rideaustin.model.enums.RideStatus;
import com.rideaustin.model.ride.Ride;
import com.rideaustin.model.ride.RideTracker;
import com.rideaustin.repo.dsl.ActiveDriverDslRepository;
import com.rideaustin.repo.dsl.RideDslRepository;
import com.rideaustin.repo.dsl.SessionDslRepository;
import com.rideaustin.rest.exception.ServerError;
import com.rideaustin.rest.model.MobileDriverRideDto;
import com.rideaustin.rest.model.RideEndLocation;
import com.rideaustin.service.ActiveDriverLocationService;
import com.rideaustin.service.FareService;
import com.rideaustin.service.MapService;
import com.rideaustin.service.RideTrackerService;
import com.rideaustin.service.SchedulerService;
import com.rideaustin.service.StackedDriverRegistry;
import com.rideaustin.service.airport.AirportService;
import com.rideaustin.service.model.DispatchCandidate;
import com.rideaustin.service.model.Events;
import com.rideaustin.service.model.States;
import com.rideaustin.service.model.context.DispatchContext;
import com.rideaustin.service.model.context.RideFlowContext;
import com.rideaustin.service.model.context.RideRequestContext;
import com.rideaustin.service.ride.RideLoadService;
import com.rideaustin.service.ride.jobs.RideSummaryJob;
import com.rideaustin.service.ride.jobs.TipReminderJob;
import com.rideaustin.utils.dispatch.StateMachineUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class EndRideAction extends AbstractContextPersistingAction implements AddressAwareAction {

  private static final Integer DEFAULT_TIP_FIRST_REMINDER_DELAY = 10;
  private static final Integer DEFAULT_TIP_SECOND_REMINDER_DELAY = 3600;

  private static final String RIDE_ID_KEY = "rideId";
  private static final String DEFAULT_TIP_FIRST_REMINDER_JOB_NAME = "TipReminderJob_%d_10s";
  private static final String DEFAULT_TIP_SECOND_REMINDER_JOB_NAME = "TipReminderJob_%d_3600s";
  private static final String RIDE_SUMMARY_JOB_NAME = "RideSummary";

  @Inject
  private RideTrackerService rideTrackerService;
  @Inject
  private AirportService airportService;
  @Inject
  private MapService mapService;
  @Inject
  private RideLoadService rideLoadService;
  @Inject
  private FareService fareService;
  @Inject
  private RideDslRepository rideDslRepository;
  @Inject
  private ActiveDriverDslRepository activeDriverDslRepository;
  @Inject
  private ActiveDriverLocationService activeDriverLocationService;
  @Inject
  private SchedulerService schedulerService;
  @Inject
  private SessionDslRepository sessionDslRepository;
  @Inject
  private StackedDriverRegistry stackedDriverRegistry;
  @Inject
  private StateMachinePersist<States, Events, String> contextAccess;

  @Override
  public void execute(StateContext<States, Events> context) {
    RideRequestContext requestContext = StateMachineUtils.getRequestContext(context);
    DispatchContext dispatchContext = StateMachineUtils.getDispatchContext(context);
    RideFlowContext flowContext = StateMachineUtils.getFlowContext(context);
    Long rideId = StateMachineUtils.getRideId(context);

    EndRideMessage contextMessage = new EndRideMessage(context.getMessageHeaders());
    RideEndLocation endLocation = contextMessage.getEndLocation();
    Date completedOn = contextMessage.getCompletedOn();

    Ride ride = rideLoadService.findOneForUpdateWithRetry(rideId);
    Address end = getAddress(endLocation, mapService);
    ride.fillEndLocation(endLocation, end);

    flowInfo(log, requestContext,"Updating status to completed");
    ride.setCompletedOn(completedOn);
    ride.setStatus(RideStatus.COMPLETED);
    ride.setDriverReachedOn(flowContext.getReachedOn());
    ride.setDriverAcceptedOn(flowContext.getAcceptedOn());
    ride.setStartedOn(flowContext.getStartedOn());
    Optional.ofNullable(flowContext.getDriverSession())
      .map(sessionDslRepository::findOne)
      .ifPresent(ride::setDriverSession);

    RideTracker rideTracker = new RideTracker(endLocation.getLat(), endLocation.getLng(),
      null, null, null, Long.MAX_VALUE);
    RideTracker lastRecord = rideTrackerService.endRide(rideId, rideTracker);
    ride.setDistanceTravelled(lastRecord.getDistanceTravelled());

    Long airportId = airportService.getAirportForLocation(requestContext.getStartLocationLat(), requestContext.getStartLocationLong())
      .map(Airport::getId).orElse(null);
    ride.setAirportId(airportId);

    fareService.calculateTotals(ride);

    rideDslRepository.save(ride);

    DeferredResult<MobileDriverRideDto> deferredResult = contextMessage.getDeferredResult();
    if (deferredResult != null) {
      deferredResult.setResult(rideDslRepository.findOneForDriver(rideId));
    }

    DispatchCandidate candidate = dispatchContext.getCandidate();
    long activeDriverId = candidate.getId();
    MobileDriverRideDto nextRide = rideDslRepository.findNextRide(activeDriverId);
    if (nextRide == null) {
      activeDriverLocationService.updateActiveDriverLocationStatus(activeDriverId, ActiveDriverStatus.AVAILABLE);
      activeDriverDslRepository.setRidingDriverAsAvailable(activeDriverId);
    } else {
      Long nextRideId = nextRide.getId();
      StateMachineContext<States, Events> nextRideContext = StateMachineUtils.getPersistedContext(environment, contextAccess, nextRideId);
      if (nextRideContext != null) {
        RideFlowContext nextRideFlowContext = StateMachineUtils.getFlowContext(nextRideContext.getExtendedState());
        nextRideFlowContext.setAcceptedOn(completedOn);
        StateMachineUtils.updateFlowContext(nextRideContext, nextRideFlowContext, environment, contextAccess);
        StateMachineUtils.updatePersistedContext(nextRideContext, environment, contextAccess, nextRideId);
      }
    }
    stackedDriverRegistry.removeFromStack(activeDriverId);

    triggerEndRideJobs(rideId, requestContext);

  }

  private void triggerEndRideJobs(Long rideId, RideRequestContext requestContext) {
    Map<String, Object> dataMap = Collections.singletonMap(RIDE_ID_KEY, rideId);
    try {
      schedulerService.triggerJob(RideSummaryJob.class, Long.toString(rideId), RIDE_SUMMARY_JOB_NAME, dataMap);

      schedulerService.triggerJob(TipReminderJob.class, String.format(DEFAULT_TIP_FIRST_REMINDER_JOB_NAME, rideId),
        DEFAULT_TIP_FIRST_REMINDER_DELAY, dataMap);

      schedulerService.triggerJob(TipReminderJob.class, String.format(DEFAULT_TIP_SECOND_REMINDER_JOB_NAME, rideId),
        DEFAULT_TIP_SECOND_REMINDER_DELAY, dataMap);
    } catch (ServerError e) {
      flowError(log, requestContext, String.format("Failed to schedule summary jobs for ride #%d", rideId), e);
    }
  }

}
