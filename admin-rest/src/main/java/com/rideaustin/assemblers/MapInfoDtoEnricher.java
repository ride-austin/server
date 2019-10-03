package com.rideaustin.assemblers;

import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.springframework.core.env.Environment;
import org.springframework.statemachine.StateMachineContext;
import org.springframework.statemachine.StateMachinePersist;
import org.springframework.stereotype.Component;

import com.rideaustin.model.enums.ActiveDriverStatus;
import com.rideaustin.model.enums.RideStatus;
import com.rideaustin.rest.model.MapInfoDto;
import com.rideaustin.service.RequestedDriversRegistry;
import com.rideaustin.service.model.Events;
import com.rideaustin.service.model.OnlineDriverDto;
import com.rideaustin.service.model.States;
import com.rideaustin.service.model.context.DispatchContext;
import com.rideaustin.utils.dispatch.StateMachineUtils;

import lombok.RequiredArgsConstructor;

@Component
public class MapInfoDtoEnricher {

  private final ActiveDriverAssembler activeDriverAssembler;
  private final StateMachinePersist<States, Events, String> contextAccess;
  private final Environment environment;

  @Inject
  public MapInfoDtoEnricher(RequestedDriversRegistry requestedDriversRegistry,
    StateMachinePersist<States, Events, String> contextAccess, Environment environment) {
    this.contextAccess = contextAccess;
    this.environment = environment;
    this.activeDriverAssembler = new ActiveDriverAssembler(requestedDriversRegistry);
  }

  public List<MapInfoDto> enrich(List<MapInfoDto> rides, List<OnlineDriverDto> drivers) {
    Map<Long, OnlineDriverDto> driversMap = drivers.stream().collect(Collectors.toMap(OnlineDriverDto::getId, Function.identity()));
    for (MapInfoDto ride : rides) {
      if (ride.getActiveDriverId() != null) {
        ride.setActiveDriver(activeDriverAssembler.toDto(driversMap.get(ride.getActiveDriverId())));
      } else if (ride.getStatus() == RideStatus.DRIVER_ASSIGNED) {
        StateMachineContext<States, Events> persistedContext = StateMachineUtils.getPersistedContext(environment, contextAccess, ride.getId());
        if (persistedContext != null) {
          DispatchContext dispatchContext = StateMachineUtils.getDispatchContext(persistedContext.getExtendedState());
          ride.setActiveDriver(activeDriverAssembler.toDto(driversMap.get(dispatchContext.getCandidate().getId())));
        }
      }
    }
    rides.addAll(
      drivers
        .stream()
        .filter(Objects::nonNull)
        .filter(ad -> EnumSet.of(ActiveDriverStatus.AVAILABLE, ActiveDriverStatus.AWAY).contains(ad.getStatus()))
        .map(activeDriverAssembler::toDto)
        .map(
          ad -> {
            MapInfoDto mapInfoDto = new MapInfoDto();
            mapInfoDto.setActiveDriver(ad);
            return mapInfoDto;
          })
        .collect(Collectors.toList())
    );
    return rides;
  }

  @RequiredArgsConstructor
  static class ActiveDriverAssembler implements SingleSideAssembler<OnlineDriverDto, MapInfoDto.ActiveDriverInfo> {

    private final RequestedDriversRegistry requestedDriversRegistry;

    @Override
    public MapInfoDto.ActiveDriverInfo toDto(OnlineDriverDto onlineDriverDto) {
      ActiveDriverStatus status = requestedDriversRegistry.isRequested(onlineDriverDto.getId()) ? ActiveDriverStatus.REQUESTED : onlineDriverDto.getStatus();
      return new MapInfoDto.ActiveDriverInfo(onlineDriverDto.getLatitude(), onlineDriverDto.getLongitude(),
        new MapInfoDto.DriverInfo(onlineDriverDto.getDriverId(), onlineDriverDto.getFullName(), onlineDriverDto.getPhoneNumber()),
        status);
    }
  }
}
