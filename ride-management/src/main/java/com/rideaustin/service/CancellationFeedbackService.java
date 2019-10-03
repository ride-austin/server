package com.rideaustin.service;

import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.MapType;
import com.fasterxml.jackson.databind.type.SimpleType;
import com.rideaustin.clients.configuration.ConfigurationItemCache;
import com.rideaustin.filter.ClientType;
import com.rideaustin.model.CancellationReasonDto;
import com.rideaustin.model.ConfigurationItem;
import com.rideaustin.model.enums.AvatarType;
import com.rideaustin.model.enums.CancellationReason;
import com.rideaustin.model.enums.RideStatus;
import com.rideaustin.model.ride.Ride;
import com.rideaustin.model.ride.RideCancellationFeedback;
import com.rideaustin.repo.dsl.CancellationFeedbackDslRepository;
import com.rideaustin.repo.dsl.RideDslRepository;
import com.rideaustin.rest.exception.BadRequestException;
import com.rideaustin.service.ride.RideOwnerService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class CancellationFeedbackService {

  private final CurrentUserService currentUserService;
  private final RideOwnerService rideOwnerService;
  private final RideDslRepository rideDslRepository;
  private final CancellationFeedbackDslRepository repository;
  private final ConfigurationItemCache configurationItemCache;
  private final ObjectMapper mapper;

  public void submit(long rideId, CancellationReason reason, AvatarType avatarType, String comment) throws BadRequestException {
    Ride ride = rideDslRepository.findOne(rideId);
    if (avatarType == AvatarType.RIDER) {
      if (ride == null || !RideStatus.RIDER_CANCELLED.equals(ride.getStatus())) {
        throw new BadRequestException("Ride is not cancelled or doesn't exist");
      }
      if (!rideOwnerService.isRideRider(rideId)) {
        throw new BadRequestException("You can't post feedback to this ride");
      }
    } else if (avatarType == AvatarType.DRIVER && !rideOwnerService.isDriversRide(rideId)) {
      throw new BadRequestException("You can't post feedback to this ride");
    }
    final long userId = currentUserService.getUser().getId();
    if (repository.noFeedbackYet(rideId, userId)) {
      repository.save(new RideCancellationFeedback(rideId, userId, reason, comment));
    } else {
      throw new BadRequestException("Feedback has been already submitted!");
    }
  }

  public List<CancellationReasonDto> listReasons(long cityId, AvatarType avatarType) {
    Optional<ConfigurationItem> cancellationReasons = configurationItemCache.getConfigurationForClient(ClientType.CONSOLE,
      "cancellationReasons", cityId);
    if (cancellationReasons.isPresent()) {
      ConfigurationItem config = cancellationReasons.get();
      MapType mapType = mapper.getTypeFactory().constructMapType(HashMap.class, CancellationReason.class, String.class);
      MapType configMapType = mapper.getTypeFactory().constructMapType(HashMap.class, SimpleType.construct(AvatarType.class), mapType);
      try {
        Map<AvatarType, Map<CancellationReason, String>> reasons = mapper.readValue(config.getConfigurationValue(), configMapType);
        return reasons.get(avatarType)
          .entrySet()
          .stream()
          .map(e -> new CancellationReasonDto(e.getKey(), e.getValue()))
          .sorted(Comparator.comparing(r -> r.getCode().ordinal()))
          .collect(Collectors.toList());
      } catch (IOException e) {
        log.error("Failed to read configuration", e);
        return createFallback();
      }
    } else {
      return createFallback();
    }
  }

  private List<CancellationReasonDto> createFallback() {
    return Arrays.stream(CancellationReason.values())
      .map(e -> new CancellationReasonDto(e, e.getDescription()))
      .sorted(Comparator.comparing(r -> r.getCode().ordinal()))
      .collect(Collectors.toList());
  }
}
