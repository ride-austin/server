package com.rideaustin.assemblers;

import java.util.Optional;
import java.util.Set;

import javax.inject.Inject;

import org.springframework.core.convert.converter.Converter;
import org.springframework.core.env.Environment;
import org.springframework.statemachine.StateMachineContext;
import org.springframework.statemachine.StateMachinePersist;
import org.springframework.stereotype.Component;

import com.rideaustin.model.Session;
import com.rideaustin.model.enums.RideStatus;
import com.rideaustin.repo.dsl.ActiveDriverDslRepository;
import com.rideaustin.repo.dsl.SessionDslRepository;
import com.rideaustin.rest.model.ExtendedRideDriverDto;
import com.rideaustin.rest.model.ExtendedRideDto;
import com.rideaustin.service.location.ObjectLocationService;
import com.rideaustin.service.location.enums.LocationType;
import com.rideaustin.service.model.DispatchCandidate;
import com.rideaustin.service.model.Events;
import com.rideaustin.service.model.OnlineDriverDto;
import com.rideaustin.service.model.States;
import com.rideaustin.service.model.context.DispatchContext;
import com.rideaustin.service.model.context.RideFlowContext;
import com.rideaustin.utils.AppInfoUtils;
import com.rideaustin.utils.dispatch.StateMachineUtils;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class ExtendedRideDtoEnricher implements DTOEnricher<ExtendedRideDto>, Converter<ExtendedRideDto, ExtendedRideDto> {

  private static final Set<RideStatus> COMPATIBLE_RIDE_STATUSES = RideStatus.ONGOING_DRIVER_STATUSES;

  private final Environment environment;
  private final SessionDslRepository sessionDslRepository;
  private final StateMachinePersist<States, Events, String> contextAccess;
  private final ObjectLocationService<OnlineDriverDto> objectLocationService;
  private final ActiveDriverDslRepository activeDriverDslRepository;

  @Override
  public ExtendedRideDto enrich(ExtendedRideDto source) {
    if (source == null) {
      return null;
    }
    StateMachineContext<States, Events> context = StateMachineUtils.getPersistedContext(environment, contextAccess, source.getRideId());
    Long activeDriverId = source.getActiveDriverId();
    if (activeDriverId == null) {
      activeDriverId = Optional.ofNullable(context)
        .map(StateMachineContext::getExtendedState)
        .map(StateMachineUtils::getDispatchContext)
        .map(DispatchContext::getCandidate)
        .map(DispatchCandidate::getId)
        .orElse(null);
    }
    Optional<RideFlowContext> flowContext = Optional.ofNullable(context)
      .map(StateMachineContext::getExtendedState)
      .map(StateMachineUtils::getFlowContext);
    if (source.getStarted() == null) {
      flowContext.map(RideFlowContext::getStartedOn).ifPresent(source::setStarted);
    }
    if (COMPATIBLE_RIDE_STATUSES.contains(source.getStatus())) {
      flowContext
        .map(RideFlowContext::getDriverSession)
        .map(sessionDslRepository::findOne)
        .map(Session::getUserAgent)
        .map(AppInfoUtils::extractVersion)
        .ifPresent(source::setDriverAppVersion);
      if (activeDriverId != null) {
        OnlineDriverDto onlineDriver = objectLocationService.getById(activeDriverId, LocationType.ACTIVE_DRIVER);
        if (onlineDriver != null) {
          String[] fullName = onlineDriver.getFullName().split(" ");
          source.setDriverFirstName(fullName[0]);
          source.setDriverLastName(fullName[1]);
          source.setDriverId(onlineDriver.getDriverId());
          source.setDriverPhoneNumber(onlineDriver.getPhoneNumber());
          source.setDriverLatitude(onlineDriver.getLatitude());
          source.setDriverLongitude(onlineDriver.getLongitude());
        } else {
          ExtendedRideDriverDto driverInfo = activeDriverDslRepository.findExtendedRideDriverInfo(activeDriverId);
          source.setDriverFirstName(driverInfo.getFirstName());
          source.setDriverLastName(driverInfo.getLastName());
          source.setDriverId(driverInfo.getDriverId());
          source.setDriverPhoneNumber(driverInfo.getPhoneNumber());
        }
        source.setActiveDriverId(activeDriverId);
      }
    }
    return source;
  }

  @Override
  public ExtendedRideDto convert(ExtendedRideDto source) {
    return enrich(source);
  }
}