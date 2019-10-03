package com.rideaustin.service.ride;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import javax.inject.Inject;

import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rideaustin.Constants;
import com.rideaustin.clients.configuration.ConfigurationItemService;
import com.rideaustin.model.ConfigurationItem;
import com.rideaustin.model.enums.RideStatus;
import com.rideaustin.model.enums.RideUpgradeRequestStatus;
import com.rideaustin.model.ride.ActiveDriver;
import com.rideaustin.model.ride.CarType;
import com.rideaustin.model.ride.CityCarType;
import com.rideaustin.model.ride.Ride;
import com.rideaustin.model.ride.RideUpgradeRequest;
import com.rideaustin.model.surgepricing.SurgeArea;
import com.rideaustin.repo.dsl.RideDslRepository;
import com.rideaustin.repo.dsl.RideUpgradeRequestDslRepository;
import com.rideaustin.repo.dsl.SurgeAreaDslRepository;
import com.rideaustin.rest.exception.BadRequestException;
import com.rideaustin.rest.exception.RideAustinException;
import com.rideaustin.rest.model.RiderDto;
import com.rideaustin.service.ActiveDriversService;
import com.rideaustin.service.event.EventsNotificationService;
import com.rideaustin.service.notifications.PushNotificationsFacade;
import com.rideaustin.service.user.CarTypesCache;
import com.rideaustin.service.user.RiderService;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class RideUpgradeService {

  private final SurgeAreaDslRepository surgeAreaDslRepository;
  private final RideUpgradeRequestDslRepository repository;
  private final RideDslRepository rideDslRepository;
  private final CarTypesCache carTypesCache;

  private final RiderService riderService;
  private final ActiveDriversService activeDriversService;
  private final ConfigurationItemService configurationItemService;
  private final PushNotificationsFacade notificationsFacade;
  private final EventsNotificationService eventsNotificationService;

  private final ObjectMapper mapper;
  private final Config config;

  @Transactional
  public void requestUpgrade(String target) throws RideAustinException {
    ActiveDriver activeDriver = activeDriversService.getCurrentActiveDriver();
    if (activeDriver == null) {
      throw new BadRequestException("You can't request ride upgrade while being offline");
    }
    long driverId = activeDriver.getDriver().getId();
    Ride ride = getCurrentRide(activeDriver);
    if (ride == null) {
      throw new BadRequestException("You can't request ride upgrade while not in a ride");
    }
    if (!RideStatus.DRIVER_REACHED.equals(ride.getStatus())) {
      throw new BadRequestException("Ride can not be upgraded");
    }
    String source = ride.getRequestedCarType().getCarCategory();
    CarType targetCarType = carTypesCache.getCarType(target);
    if (targetCarType == null) {
      throw new BadRequestException("Specified car category doesn't exist");
    }
    Optional<UpgradeConfig> upgradeConfig = getUpgradeConfig(ride.getCityId());
    if (!upgradeConfig.isPresent() || !upgradeConfig.get().supportsUpgrade(source, target)) {
      throw new BadRequestException(String.format("Ride upgrade from %s to %s is not supported", source, target));
    }
    if (repository.alreadyRequestedForRide(driverId, ride.getId())) {
      throw new BadRequestException("You can't request upgrade for a ride more than once");
    }

    BigDecimal surgeFactor = Constants.NEUTRAL_SURGE_FACTOR;
    if (ride.getStartAreaId() != null) {
      SurgeArea surgeArea = surgeAreaDslRepository.findByAreaGeometry(ride.getStartAreaId());
      surgeFactor = surgeArea.getSurgeFactor(targetCarType);
    }

    RideUpgradeRequest request = RideUpgradeRequest.builder()
      .expiresOn(Date.from(Instant.now().plus(config.expirationTimeout, ChronoUnit.SECONDS)))
      .requestedBy(driverId)
      .requestedFrom(ride.getRider().getId())
      .rideId(ride.getId())
      .source(source)
      .target(target)
      .status(RideUpgradeRequestStatus.REQUESTED)
      .surgeFactor(surgeFactor)
      .build();
    repository.save(request);

    notificationsFacade.pushRideUpgradeRequest(ride.getId(), ride.getRider().getUser(), source, target, surgeFactor);
  }

  @Transactional
  public boolean acceptRequest() throws RideAustinException {
    RiderDto currentRider = riderService.getCurrentRider();
    RideUpgradeRequest request = repository.findByRiderAndStatus(currentRider.getId(),
      RideUpgradeRequestStatus.REQUESTED, RideUpgradeRequestStatus.CANCELLED, RideUpgradeRequestStatus.EXPIRED);
    if (request != null) {
      Ride ride = rideDslRepository.findOne(request.getRideId());
      if (ride == null || ride.getActiveDriver() == null || !Objects.equals(ride.getActiveDriver().getDriver().getId(), request.getRequestedBy())) {
        return false;
      }
      request.setStatus(RideUpgradeRequestStatus.ACCEPTED);
      repository.save(request);
      ride = updateCarCategory(request.getRideId(), request.getTarget(), request.getSurgeFactor());
      eventsNotificationService.sendRideUpgradeAccepted(ride, request.getRequestedBy());
      return true;
    }
    return false;
  }

  @Transactional
  public boolean cancelRequest() throws RideAustinException {
    ActiveDriver activeDriver = activeDriversService.getCurrentActiveDriver();
    if (activeDriver == null) {
      throw new BadRequestException("You can not cancel upgrade request while being offline");
    }
    RideUpgradeRequest request = repository.findByDriverAndStatus(activeDriver.getDriver().getId(), RideUpgradeRequestStatus.REQUESTED);
    if (request != null) {
      request.setStatus(RideUpgradeRequestStatus.CANCELLED);
      repository.save(request);
      return true;
    }
    return false;
  }

  @Transactional
  public boolean declineRequest() throws RideAustinException {
    RiderDto currentRider = riderService.getCurrentRider();
    RideUpgradeRequest request = repository.findByRiderAndStatus(currentRider.getId(), RideUpgradeRequestStatus.REQUESTED, RideUpgradeRequestStatus.EXPIRED);
    if (request != null) {
      request.setStatus(RideUpgradeRequestStatus.DECLINED);
      repository.save(request);
      eventsNotificationService.sendRideUpgradeDeclined(request.getRideId(), request.getRequestedBy());
      return true;
    }
    return false;
  }

  @Transactional
  public void expireRequests() {
    List<RideUpgradeRequest> expired = repository.findExpired();
    expired.forEach(r -> r.setStatus(RideUpgradeRequestStatus.EXPIRED));
    repository.saveMany(expired);
    for (RideUpgradeRequest request : expired) {
      eventsNotificationService.sendRideUpgradeDeclined(request.getRideId(), request.getRequestedBy());
    }
  }

  public Optional<RideUpgradeRequest> getRequest(Long rideId, Long driverId) {
    if (driverId == null) {
      return Optional.empty();
    } else {
      return repository.findByRideAndDriver(rideId, driverId);
    }
  }

  private Ride getCurrentRide(ActiveDriver activeDriver) {
    Set<RideStatus> statuses = EnumSet.of(RideStatus.DRIVER_ASSIGNED, RideStatus.DRIVER_REACHED);
    return rideDslRepository.findByDriverAndStatus(activeDriver, statuses);
  }

  private Ride updateCarCategory(long rideId, String target, BigDecimal surgeFactor) {
    Ride ride = rideDslRepository.findOne(rideId);
    CityCarType cityCarType = carTypesCache.getCityCarType(ride.getCityId(), target);
    if (cityCarType != null) {
      if (ride.getStartAreaId() != null) {
        ride.setSurgeFactor(surgeFactor);
      }
      ride.setRequestedCarType(cityCarType.getCarType());
      rideDslRepository.save(ride);
    }
    return ride;
  }

  private Optional<UpgradeConfig> getUpgradeConfig(Long cityId) {
    try {
      ConfigurationItem upgradeConfig = configurationItemService.findByKeyAndCityId("rideUpgrade", cityId);
      if (upgradeConfig == null) {
        return Optional.empty();
      }
      return Optional.ofNullable(mapper.readValue(upgradeConfig.getConfigurationValue(), UpgradeConfig.class));
    } catch (IOException e) {
      log.error("Failed to parse configuration", e);
    }
    return Optional.empty();
  }

  @Component
  public static class Config {
    final int expirationTimeout;

    @Inject
    public Config(Environment environment) {
      this.expirationTimeout = environment.getProperty("ride.upgrade.request.expiration.timeout", Integer.class, 45);
    }
  }

  @Getter
  @Setter
  @JsonIgnoreProperties(ignoreUnknown = true)
  static class UpgradeConfig {
    List<Variant> variants;

    @Getter
    @Setter
    static class Variant {
      String carCategory;
      Set<String> validUpgrades;
    }

    @JsonIgnore
    public boolean supportsUpgrade(String source, String target) {
      boolean supports = false;
      if (variants != null) {
        for (Variant variant : variants) {
          if (variant.getCarCategory().equalsIgnoreCase(source)) {
            return variant.getValidUpgrades().contains(target);
          }
        }
      }
      return supports;
    }
  }
}
