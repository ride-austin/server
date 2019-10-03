package com.rideaustin.assemblers;

import java.util.Optional;

import javax.inject.Inject;

import org.springframework.core.env.Environment;
import org.springframework.statemachine.StateMachineContext;
import org.springframework.statemachine.StateMachinePersist;
import org.springframework.stereotype.Component;

import com.rideaustin.model.enums.RideStatus;
import com.rideaustin.model.ride.RideUpgradeRequest;
import com.rideaustin.repo.dsl.RideDslRepository;
import com.rideaustin.rest.model.MobileDriverRideDto;
import com.rideaustin.rest.model.MobileDriverRideDto.RequestedDispatchType;
import com.rideaustin.rest.model.RideUpgradeRequestDto;
import com.rideaustin.service.StackedDriverRegistry;
import com.rideaustin.service.model.DispatchCandidate;
import com.rideaustin.service.model.Events;
import com.rideaustin.service.model.States;
import com.rideaustin.service.model.context.DispatchContext;
import com.rideaustin.service.model.context.RideRequestContext;
import com.rideaustin.service.ride.RideUpgradeService;
import com.rideaustin.utils.dispatch.StateMachineUtils;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class MobileDriverRideDtoEnricher implements DTOEnricher<MobileDriverRideDto> {

  private final RideUpgradeService upgradeService;
  private final StateMachinePersist<States, Events, String> contextAccess;
  private final Environment environment;
  private final StackedDriverRegistry stackedDriverRegistry;
  private final RideDslRepository rideDslRepository;

  @Override
  public MobileDriverRideDto enrich(MobileDriverRideDto source) {
    if (source == null) {
      return null;
    }
    Optional<RideUpgradeRequest> upgradeRequest = upgradeService.getRequest(source.getId(), source.getDriverId());
    upgradeRequest.map(RideUpgradeRequestDto::new).ifPresent(source::setUpgradeRequest);

    StateMachineContext<States, Events> context = StateMachineUtils.getPersistedContext(environment, contextAccess, source.getId());
    if (context != null) {
      DispatchCandidate candidate = Optional.ofNullable(StateMachineUtils.getDispatchContext(context.getExtendedState())).map(DispatchContext::getCandidate).orElse(null);
      if (candidate != null) {
        source.setEstimatedTimeArrive(candidate.getDrivingTimeToRider());
        if (hasStackedRide(source, candidate)) {
          MobileDriverRideDto nextRide = rideDslRepository.findNextRide(candidate.getId());
          this.enrich(nextRide);
          source.setNextRide(nextRide);
        }
      }
      RideRequestContext requestContext = StateMachineUtils.getRequestContext(context.getExtendedState());
      if (requestContext != null) {
        if (requestContext.getDirectConnectId() != null) {
          source.setRequestedDispatchType(RequestedDispatchType.DIRECT_CONNECT);
        } else {
          source.setRequestedDispatchType(RequestedDispatchType.REGULAR);
        }
      }
    }

    return source;
  }

  private boolean hasStackedRide(MobileDriverRideDto source, DispatchCandidate candidate) {
    return RideStatus.ACTIVE.equals(source.getStatus()) && source.getNextRide() == null && stackedDriverRegistry.isStacked(candidate.getId());
  }
}
