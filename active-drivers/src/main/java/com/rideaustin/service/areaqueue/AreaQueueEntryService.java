package com.rideaustin.service.areaqueue;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import org.springframework.stereotype.Service;

import com.rideaustin.model.Area;
import com.rideaustin.model.AreaQueueEntry;
import com.rideaustin.repo.dsl.AreaQueueEntryDslRepository;
import com.rideaustin.service.RequestedDriversRegistry;
import com.rideaustin.service.location.ObjectLocationService;
import com.rideaustin.service.location.enums.LocationType;
import com.rideaustin.service.model.OnlineDriverDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class AreaQueueEntryService {

  private final AreaQueueEntryDslRepository areaQueueEntryDslRepository;
  private final ObjectLocationService<OnlineDriverDto> objectLocationService;
  private final RequestedDriversRegistry requestedDriversRegistry;

  public List<AreaQueueEntry> getCurrentActiveDriverAreaQueueEntry(final long activeDriverId) {
    return areaQueueEntryDslRepository.findEnabledByActiveDriver(activeDriverId);
  }

  public List<AreaQueueEntry> getEntries(@Nonnull Area area) {
    return areaQueueEntryDslRepository.findByArea(area);
  }

  public List<OnlineDriverDto> getAvailableActiveDriversFromArea(Area area, List<Long> ignoreIds, String carCategory) {
    Predicate<Long> requestedPredicate = requestedDriversRegistry::isRequested;
    requestedPredicate = requestedPredicate.negate();
    final List<Long> ids = areaQueueEntryDslRepository.findQueuedAvailableActiveDriverIds(area, ignoreIds, carCategory)
      .stream()
      .distinct()
      .filter(requestedPredicate)
      .collect(Collectors.toList());
    return objectLocationService.getByIds(ids, LocationType.ACTIVE_DRIVER);
  }

  public List<AreaQueueEntry> getCurrentActiveDriverAreaQueueEntry(long id, Area area) {
    return areaQueueEntryDslRepository.findEnabledByActiveDriver(id, area);
  }
}
