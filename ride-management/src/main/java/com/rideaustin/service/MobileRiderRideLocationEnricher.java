package com.rideaustin.service;

import static com.rideaustin.utils.SafeZeroUtils.safeZero;

import java.util.Optional;

import javax.inject.Inject;

import org.springframework.core.env.Environment;
import org.springframework.statemachine.ExtendedState;
import org.springframework.statemachine.StateMachineContext;
import org.springframework.statemachine.StateMachinePersist;
import org.springframework.stereotype.Component;

import com.rideaustin.assemblers.DTOEnricher;
import com.rideaustin.model.ride.RideUpgradeRequest;
import com.rideaustin.repo.dsl.RideDslRepository;
import com.rideaustin.rest.model.MobileRiderRideDto;
import com.rideaustin.rest.model.RideUpgradeRequestDto;
import com.rideaustin.service.location.ObjectLocationService;
import com.rideaustin.service.location.enums.LocationType;
import com.rideaustin.service.model.DispatchCandidate;
import com.rideaustin.service.model.Events;
import com.rideaustin.service.model.OnlineDriverDto;
import com.rideaustin.service.model.States;
import com.rideaustin.service.model.context.DispatchContext;
import com.rideaustin.service.ride.RideUpgradeService;
import com.rideaustin.utils.dispatch.StateMachineUtils;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class MobileRiderRideLocationEnricher<T extends MobileRiderRideDto> implements DTOEnricher<T> {

  protected final StateMachinePersist<States, Events, String> contextAccess;
  protected final Environment environment;
  private final RideUpgradeService upgradeService;
  private final ObjectLocationService<OnlineDriverDto> objectLocationService;
  protected final RideDslRepository rideDslRepository;

  @Override
  public T enrich(T source) {
    if (source == null) {
      return null;
    }
    DispatchCandidate candidate = null;
    StateMachineContext<States, Events> persistedContext = StateMachineUtils.getPersistedContext(environment, contextAccess, source.getId());
    if (persistedContext != null) {
      ExtendedState extendedState = persistedContext.getExtendedState();
      candidate = Optional.ofNullable(StateMachineUtils.getDispatchContext(extendedState)).map(DispatchContext::getCandidate).orElse(null);
    }
    if (source.getActiveDriver() == null && candidate != null) {
      OnlineDriverDto driver = objectLocationService.getById(candidate.getId(), LocationType.ACTIVE_DRIVER);
      source.setActiveDriver(rideDslRepository.getActiveDriverForRider(candidate.getId()));
      if (driver != null && driver.getLocationObject() != null) {
        source.getActiveDriver().setCourse(safeZero(driver.getLocationObject().getCourse()));
        source.getActiveDriver().setLatitude(driver.getLatitude());
        source.getActiveDriver().setLongitude(driver.getLongitude());
      }
    }
    if (source.getActiveDriver() != null) {
      OnlineDriverDto driver = objectLocationService.getById(source.getActiveDriver().getId(), LocationType.ACTIVE_DRIVER);
      if (driver != null && driver.getLocationObject() != null) {
        source.getActiveDriver().setCourse(safeZero(driver.getLocationObject().getCourse()));
        source.getActiveDriver().setLatitude(driver.getLatitude());
        source.getActiveDriver().setLongitude(driver.getLongitude());
      } else if (candidate != null) {
        source.getActiveDriver().setLatitude(candidate.getLatitude());
        source.getActiveDriver().setLongitude(candidate.getLongitude());
      }
      Optional<RideUpgradeRequest> upgradeRequest = upgradeService.getRequest(source.getId(), source.getActiveDriver().getDriver().getId());
      upgradeRequest.map(RideUpgradeRequestDto::new).ifPresent(source::setUpgradeRequest);
    }
    return source;
  }
}
