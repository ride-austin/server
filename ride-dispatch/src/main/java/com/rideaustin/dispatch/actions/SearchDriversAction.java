package com.rideaustin.dispatch.actions;

import static com.rideaustin.dispatch.LogUtil.dispatchInfo;

import java.sql.Date;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.StateMachineContext;
import org.springframework.statemachine.StateMachinePersist;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.persist.DefaultStateMachinePersister;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.annotations.VisibleForTesting;
import com.google.maps.model.LatLng;
import com.rideaustin.dispatch.tasks.PreauthorizationTask;
import com.rideaustin.model.Area;
import com.rideaustin.model.enums.ActiveDriverStatus;
import com.rideaustin.model.ride.CityDriverType;
import com.rideaustin.model.ride.DriverTypeSearchHandler;
import com.rideaustin.repo.dsl.ActiveDriverDslRepository;
import com.rideaustin.service.ActiveDriverLocationService;
import com.rideaustin.service.ActiveDriverSearchCriteria;
import com.rideaustin.service.DefaultSearchDriverHandler;
import com.rideaustin.service.MapService;
import com.rideaustin.service.QueuedActiveDriverSearchCriteria;
import com.rideaustin.service.RequestedDriversRegistry;
import com.rideaustin.service.StackedDriverRegistry;
import com.rideaustin.service.areaqueue.AreaService;
import com.rideaustin.service.config.ActiveDriverServiceConfig;
import com.rideaustin.service.config.RideDispatchServiceConfig;
import com.rideaustin.service.config.RidePaymentConfig;
import com.rideaustin.service.model.DispatchCandidate;
import com.rideaustin.service.model.Events;
import com.rideaustin.service.model.OnlineDriverDto;
import com.rideaustin.service.model.States;
import com.rideaustin.service.model.context.DispatchContext;
import com.rideaustin.service.model.context.DispatchType;
import com.rideaustin.service.model.context.RideRequestContext;
import com.rideaustin.service.ride.DriverTypeService;
import com.rideaustin.utils.dispatch.StateMachineUtils;
import com.sromku.polygon.Point;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SearchDriversAction implements Action<States, Events> {

  @Inject
  private BeanFactory beanFactory;
  @Inject
  @Named("taskExecutor")
  private TaskScheduler taskScheduler;
  @Inject
  private ObjectMapper objectMapper;
  @Inject
  private DriverTypeService driverTypeService;
  @Inject
  private ActiveDriverServiceConfig searchConfig;
  @Inject
  private AreaService areaService;
  @Inject
  private RideDispatchServiceConfig config;
  @Inject
  private Environment environment;
  @Inject
  private StateMachinePersist<States, Events, String> access;
  @Inject
  private RequestedDriversRegistry requestedDriversRegistry;
  @Inject
  private ActiveDriverDslRepository activeDriverDslRepository;
  @Inject
  private StackedDriverRegistry stackedDriverRegistry;
  @Inject
  private DefaultStateMachinePersister<States, Events, String> persister;
  @Inject
  private MapService mapService;
  @Inject
  private ActiveDriverLocationService activeDriverLocationService;
  @Inject
  private RidePaymentConfig ridePaymentConfig;

  @Override
  public void execute(StateContext<States, Events> context) {
    RideRequestContext requestContext = StateMachineUtils.getRequestContext(context);
    if (context.getSource() == null) {
      dispatchInfo(log, requestContext, "SearchDriverAction started with machine start");
    } else {
      dispatchInfo(log, requestContext, String.format("SearchDriverAction started after transition from %s", context.getSource().getId()));
    }

    Long rideId = StateMachineUtils.getRideId(context);
    StateMachineContext<States, Events> persistedContext = StateMachineUtils.getPersistedContext(environment, access, rideId);

    dispatchInfo(log, requestContext, String.format("Found persisted machine context: %s", persistedContext != null));
    requestContext = getRideRequestContext(context, persistedContext);

    if (noDispatchInProgress(context, persistedContext)) {
      if (dispatchNotExpired(requestContext)) {
        dispatchLoop(context, requestContext);
      } else {
        dispatchInfo(log, requestContext, "Dispatch expired, sending NAD from SearchAction");
        context.getStateMachine().sendEvent(Events.NO_DRIVERS_AVAILABLE);
      }
    } else {
      dispatchInfo(log, requestContext, "Dispatch request is in progress, aborting action");
    }
  }

  private void dispatchLoop(StateContext<States, Events> context, RideRequestContext requestContext) {
    Integer driverSearchRadius = getDriverSearchRadius(context);

    List<Long> ignoreIds = new ArrayList<>(requestContext.getIgnoreIds());
    Double startLocationLat = requestContext.getStartLocationLat();
    Double startLocationLong = requestContext.getStartLocationLong();
    Long cityId = requestContext.getCityId();
    Integer requestedDriverType = requestContext.getRequestedDriverTypeBitmask();
    String requestedCarCategory = requestContext.getRequestedCarTypeCategory();
    String directConnectId = requestContext.getDirectConnectId();

    Area area = areaService.isInArea(new LatLng(startLocationLat, startLocationLong), cityId);
    // use top-level area for inner areas that are not visible to drivers (e.g. staging area inside airport)
    if (area != null && !area.isVisibleToDrivers() && area.getParentAreaId() != null) {
      area = areaService.getById(area.getParentAreaId());
    }
    List<OnlineDriverDto> activeDrivers = new ArrayList<>();
    DriverTypeSearchHandler searchHandler = resolveDriverSearchHandler(requestContext);
    DispatchType dispatchType = DispatchType.REGULAR;
    if (area != null) {
      dispatchInfo(log, requestContext, String.format("Searching by queue. Ignoring drivers: %s", ignoreIds));
      activeDrivers = searchHandler.searchDrivers(new QueuedActiveDriverSearchCriteria(area, ignoreIds,
        requestedCarCategory, requestedDriverType, directConnectId));
      dispatchType = DispatchType.QUEUED;
    }
    boolean dispatchSent = false;
    while (!dispatchSent && driverSearchRadius < config.getDriverSearchRadiusLimit()) {
      while (activeDrivers.isEmpty() && driverSearchRadius < config.getDriverSearchRadiusLimit()) {
        dispatchInfo(log, requestContext, String.format("Searching by location, driverSearchRadius %d; ignoring drivers: %s", driverSearchRadius, ignoreIds));
        activeDrivers = searchHandler.searchDrivers(new ActiveDriverSearchCriteria(startLocationLat, startLocationLong,
          ignoreIds, searchConfig.getNumberOfEtaDrivers(), requestedCarCategory,
          requestContext.getRequestedCarTypeBitmask(), cityId, requestedDriverType, driverSearchRadius, null,
          directConnectId));
        dispatchInfo(log, requestContext, String.format("Found %d candidates", activeDrivers.size()));
        activeDrivers = activeDrivers
          .stream()
          .filter(this::eligibleForDispatch)
          .collect(Collectors.toList());
        driverSearchRadius += config.getDriverSearchRadiusStep();
        dispatchType = DispatchType.REGULAR;
      }

      if (driverSearchRadius <= config.getDriverSearchRadiusLimit() && !activeDrivers.isEmpty()) {
        dispatchInfo(log, requestContext, "Proceeding to send dispatch request step");
        if (ridePaymentConfig.isAsyncPreauthEnabled()) {
          dispatchInfo(log, requestContext, "Launching preauthorization task");
          taskScheduler.schedule(
            beanFactory.getBean(PreauthorizationTask.class)
              .withApplePayToken(requestContext.getApplePayToken())
              .withRideId(requestContext.getRideId()),
            Date.from(Instant.now().plus(5, ChronoUnit.MILLIS)));
        }
        dispatchSent = dispatchToFirstDriver(context, new DispatchContext(requestContext.getRideId(), cityId, startLocationLat, startLocationLong, dispatchType), activeDrivers);
        activeDrivers.clear();
      }
    }
    if (!dispatchSent) {
      dispatchInfo(log, requestContext, "Search radius is exhausted, sending NAD from SearchAction");
      context.getStateMachine().sendEvent(Events.NO_DRIVERS_AVAILABLE);
    }
  }

  private boolean eligibleForDispatch(OnlineDriverDto driver) {
    return !requestedDriversRegistry.isRequested(driver.getId())
      && (driver.getStatus() == ActiveDriverStatus.AVAILABLE || driver.isEligibleForStacking())
      && !stackedDriverRegistry.isStacked(driver.getId());
  }

  private DriverTypeSearchHandler resolveDriverSearchHandler(RideRequestContext requestContext) {
    Optional<String> optionalHandler = driverTypeService.getCityDriverType(requestContext.getRequestedDriverTypeBitmask(), requestContext.getCityId())
      .map(cdt -> cdt.getConfigurationObject(objectMapper))
      .map(CityDriverType.Configuration::getSearchHandlerClass);
    Class<? extends DriverTypeSearchHandler> handlerClass;
    if (optionalHandler.isPresent()) {
      try {
        handlerClass = (Class<? extends DriverTypeSearchHandler>) Class.forName(optionalHandler.get());
      } catch (Exception e) {
        log.error("Failed to load class " + optionalHandler.get());
        handlerClass = DefaultSearchDriverHandler.class;
      }
    } else {
      handlerClass = DefaultSearchDriverHandler.class;
    }
    DriverTypeSearchHandler searchHandler = beanFactory.getBean(handlerClass);
    dispatchInfo(log, requestContext, String.format("Processing direct connect request: %s", requestContext.getDirectConnectId() != null));
    Optional.ofNullable(requestContext.getDirectConnectId()).ifPresent(s -> dispatchInfo(log, requestContext, String.format("DCID requested: %s", s)));
    dispatchInfo(log, requestContext, String.format("Search will be performed with %s", handlerClass.getSimpleName()));
    return searchHandler;
  }

  private boolean dispatchToFirstDriver(StateContext<States, Events> stateContext, DispatchContext context, List<OnlineDriverDto> drivers) {
    dispatchInfo(log, context.getId(), String.format("Dispatching to %d candidates", drivers.size()));
    boolean dispatchSent = false;
    for (int i = 0, driversSize = drivers.size(); i < driversSize && !dispatchSent; i++) {
      OnlineDriverDto driver = drivers.get(i);
      dispatchInfo(log, context.getId(), String.format("Dispatch for ad %d", driver.getId()));

      DispatchCandidate dispatchCandidate = activeDriverDslRepository.findDispatchCandidate(driver.getId());
      dispatchCandidate.update(driver);

      final RideRequestContext requestContext = StateMachineUtils.getRequestContext(stateContext);
      boolean drivingTimeNotUpdated = !updateDrivingTimeToRider(context, dispatchCandidate);
      if (requestContext.getDirectConnectId() == null) {
        boolean noEtaBasedDispatch = !shouldDispatchBasedOnEta(dispatchCandidate);
        if (drivingTimeNotUpdated || noEtaBasedDispatch) {
          dispatchInfo(log, context.getId(), String.format("Skipping dispatch due to: [drivingTimeNotUpdated : %s, noEtaBasedDispatch : %s]", drivingTimeNotUpdated, noEtaBasedDispatch));
          continue;
        }
      }
      dispatchInfo(log, requestContext, String.format("[RA15196] Dispatching %d ride to %d driver",
        requestContext.getRequestedCarTypeBitmask(), driver.getAvailableCarCategoriesBitmask()));
      if (dispatchDriver(stateContext, context, dispatchCandidate)) {
        dispatchSent = true;
      }
    }
    return dispatchSent;
  }

  private boolean updateDrivingTimeToRider(DispatchContext context, DispatchCandidate candidate) {
    if (candidate.getDrivingTimeToRider() == null) {
      candidate.setDrivingTimeToRider(mapService.getTimeToDrive(new LatLng(candidate.getLatitude(), candidate.getLongitude()), new LatLng(context.getStartLocationLat(), context.getStartLocationLng())));
    }
    return candidate.getDrivingTimeToRider() != null;
  }

  private boolean shouldDispatchBasedOnEta(DispatchCandidate ad) {
    if (ad.getDrivingTimeToRider() != null) {
      if (config.getCityCenterDispatchPolygon().contains(new Point((float) ad.getLatitude(), (float) ad.getLongitude()))) {
        return ad.getDrivingTimeToRider() <= config.getDriverCityCenterMaxEtaTime();
      } else {
        return ad.getDrivingTimeToRider() <= config.getDriverMaxEtaTime();
      }
    } else {
      return false;
    }
  }

  private boolean dispatchDriver(StateContext<States, Events> stateContext, DispatchContext context, DispatchCandidate candidate) {
    if (stateContext.getStateMachine() != null) {
      context.setCandidate(candidate);
      StateMachineUtils.updateDispatchContext(stateContext, context, persister, environment);
      RideRequestContext requestContext = StateMachineUtils.getRequestContext(stateContext.getExtendedState());
      requestContext.getIgnoreIds().add(candidate.getId());
      StateMachineUtils.updateRequestContext(stateContext, requestContext, persister, environment);
      Events nextEvent = Events.HANDSHAKE_REQUEST_SEND;
      if (stateContext.getStateMachine().sendEvent(nextEvent)) {
        requestedDriversRegistry.addRequested(candidate.getId());
        if (candidate.isStacked()) {
          stackedDriverRegistry.addStacked(candidate.getId());
          activeDriverLocationService.updateActiveDriverStackedEligibility(candidate.getId(), false);
        }
      } else {
        return false;
      }
    } else {
      return false;
    }
    return true;
  }

  private boolean noDispatchInProgress(StateContext<States, Events> context, StateMachineContext<States, Events> persistedContext) {
    return !getDispatchContext(context, persistedContext).map(DispatchContext::getCandidate).isPresent();
  }

  private RideRequestContext getRideRequestContext(StateContext<States, Events> context, StateMachineContext<States, Events> persistedContext) {
    return persistedContext == null ?
      StateMachineUtils.getRequestContext(context) :
      StateMachineUtils.getRequestContext(persistedContext.getExtendedState());
  }

  private Optional<DispatchContext> getDispatchContext(StateContext<States, Events> context, StateMachineContext<States, Events> persistedContext) {
    return persistedContext == null ?
      Optional.ofNullable(StateMachineUtils.getDispatchContext(context)) :
      Optional.ofNullable(StateMachineUtils.getDispatchContext(persistedContext.getExtendedState()));
  }

  private Integer getDriverSearchRadius(StateContext<States, Events> context) {
    return StateMachineUtils.getRequestContext(context).getDriverSearchRadius();
  }

  private boolean dispatchNotExpired(RideRequestContext requestContext) {
    return System.currentTimeMillis() - requestContext.getCreatedDate().getTime() < config.getTotalDispatchWaitTime(requestContext.getCityId()) * 1000;
  }

  @VisibleForTesting
  void setObjectMapper(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }
}
