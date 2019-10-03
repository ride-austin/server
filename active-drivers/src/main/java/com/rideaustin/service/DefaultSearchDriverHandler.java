package com.rideaustin.service;

import static com.rideaustin.utils.SafeZeroUtils.safeZero;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rideaustin.model.Area;
import com.rideaustin.model.ride.CityDriverType;
import com.rideaustin.model.ride.DriverType;
import com.rideaustin.model.ride.DriverTypeSearchHandler;
import com.rideaustin.repo.dsl.ActiveDriverDslRepository;
import com.rideaustin.service.areaqueue.AreaQueueEntryService;
import com.rideaustin.service.config.ActiveDriverServiceConfig;
import com.rideaustin.service.model.OnlineDriverDto;
import com.rideaustin.service.user.DriverTypeCache;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
public class DefaultSearchDriverHandler implements DriverTypeSearchHandler {

  private final DriverTypeCache driverTypeCache;
  private final ActiveDriverDslRepository activeDriverDslRepository;

  private final ActiveDriverLocationService activeDriverLocationService;
  private final AreaQueueEntryService areaQueueEntryService;
  private final UpdateDistanceTimeService updateDistanceTimeService;
  private final Comparator<OnlineDriverDto> onlineDriverDtoComparator;

  @Inject
  public DefaultSearchDriverHandler(ActiveDriverServiceConfig config, DriverTypeCache driverTypeCache,
    ActiveDriverDslRepository activeDriverDslRepository, ActiveDriverLocationService activeDriverLocationService,
    AreaQueueEntryService areaQueueEntryService, UpdateDistanceTimeService updateDistanceTimeService) {
    this.driverTypeCache = driverTypeCache;
    this.activeDriverDslRepository = activeDriverDslRepository;
    this.activeDriverLocationService = activeDriverLocationService;
    this.areaQueueEntryService = areaQueueEntryService;
    this.updateDistanceTimeService = updateDistanceTimeService;
    this.onlineDriverDtoComparator = new FingerprintPriorityComparator(
      driverTypeCache.toBitMask(Collections.singleton(DriverType.FINGERPRINTED)),
      config.getFingerprintedDriverHandicap()
    );
  }

  @Override
  public List<OnlineDriverDto> searchDrivers(ActiveDriverSearchCriteria searchCriteria) {
    List<OnlineDriverDto> nearbyActiveDrivers = activeDriverLocationService.locationAround(searchCriteria.getLatitude(),
      searchCriteria.getLongitude(),
      searchCriteria.getSearchRadius(),
      searchCriteria.getCarCategory(),
      searchCriteria.getLimit());

    Map<Long, Integer> grantedTypes = getGrantedDriverTypes(searchCriteria, nearbyActiveDrivers);

    DriverFilter driverFilter = new CompositeFilter(new IgnoredDriverFilter(searchCriteria),
      new DriverTypeDriverFilter(driverTypeCache, searchCriteria, grantedTypes), new CarTypeDriverFilter(searchCriteria));

    List<OnlineDriverDto> finalFilteredAD = nearbyActiveDrivers
      .stream()
      .filter(Objects::nonNull)
      .filter(driverFilter::filter)
      .collect(Collectors.toList());

    updateDistanceTimeService.updateDistanceTimeWithETC(searchCriteria.getLatitude(), searchCriteria.getLongitude(),
      finalFilteredAD, searchCriteria.getUpdateTimeToDriveCount(), OnlineDriverDto::isEligibleForStacking);

    applyPrioritySorting(finalFilteredAD, searchCriteria);

    return finalFilteredAD;
  }

  @Override
  public List<OnlineDriverDto> searchDrivers(QueuedActiveDriverSearchCriteria searchCriteria) {
    String carCategory = searchCriteria.getCarCategory();
    Area area = searchCriteria.getArea();
    final List<OnlineDriverDto> driversInArea = areaQueueEntryService.getAvailableActiveDriversFromArea(area,
      searchCriteria.getIgnoreIds(), carCategory);
    DriverFilter driverFilter = new DriverTypeDriverFilter(driverTypeCache, searchCriteria, getGrantedDriverTypes(searchCriteria, driversInArea));
    return driversInArea
      .stream()
      .filter(Objects::nonNull)
      .filter(driverFilter::filter)
      .collect(Collectors.toList());
  }

  private Map<Long, Integer> getGrantedDriverTypes(BaseActiveDriverSearchCriteria searchCriteria, List<OnlineDriverDto> drivers) {
    Map<Long, Integer> grantedTypes;
    if (searchCriteria.getDriverTypeBitmask() == null) {
      grantedTypes = Collections.emptyMap();
    } else {
      List<Long> nearbyActiveDriverIds =
        drivers.stream().map(OnlineDriverDto::getId).collect(Collectors.toList());
      grantedTypes = activeDriverDslRepository.findDriverTypeGrantedActiveDriverIds(nearbyActiveDriverIds);
    }
    return grantedTypes;
  }

  private void applyPrioritySorting(List<OnlineDriverDto> drivers, ActiveDriverSearchCriteria searchCriteria) {
    final Integer fingerPrintedBitmask = driverTypeCache.toBitMask(Collections.singleton(DriverType.FINGERPRINTED));
    if ((safeZero(searchCriteria.getDriverTypeBitmask()) & fingerPrintedBitmask) > 0) {
      drivers.sort(onlineDriverDtoComparator);
    }
  }
  static class FingerprintPriorityComparator implements Comparator<OnlineDriverDto> {

    private final Integer fingerPrintedBitmask;
    private final Integer handicap;

    FingerprintPriorityComparator(final Integer fingerPrintedBitmask, final Integer handicap) {
      this.fingerPrintedBitmask = fingerPrintedBitmask;
      this.handicap = handicap;
    }

    @Override
    public int compare(OnlineDriverDto o1, OnlineDriverDto o2) {
      if (o1.getDrivingTimeToRider() != null && o2.getDrivingTimeToRider() != null && Math.abs(o1.getDrivingTimeToRider() - o2.getDrivingTimeToRider()) < handicap) {
        if ((o1.getAvailableDriverTypesBitmask() & fingerPrintedBitmask) == 0 && (o2.getAvailableDriverTypesBitmask() & fingerPrintedBitmask) > 0) {
          return 1;
        } else if ((o1.getAvailableDriverTypesBitmask() & fingerPrintedBitmask) == (o2.getAvailableDriverTypesBitmask() & fingerPrintedBitmask)) {
          return o1.getDrivingTimeToRider().compareTo(o2.getDrivingTimeToRider());
        } else {
          return -1;
        }
      }
      return o1.getDrivingTimeToRider() != null ? o1.getDrivingTimeToRider().compareTo(o2.getDrivingTimeToRider()) : 0;
    }
  }

  @AllArgsConstructor
  abstract static class DriverFilter<T extends BaseActiveDriverSearchCriteria> {
    protected final T searchCriteria;

    public abstract boolean filter(OnlineDriverDto activeDriver);
  }

  static class IgnoredDriverFilter extends DriverFilter {

    IgnoredDriverFilter(ActiveDriverSearchCriteria searchCriteria) {
      super(searchCriteria);
    }

    @Override
    public boolean filter(OnlineDriverDto activeDriver) {
      return searchCriteria.getIgnoreIds() == null || !searchCriteria.getIgnoreIds().contains(activeDriver.getId());
    }
  }

  static class DriverTypeDriverFilter extends DriverFilter<BaseActiveDriverSearchCriteria> {

    private final Map<Long, Integer> grantedTypes;
    private final DriverTypeCache driverTypeCache;
    private final ObjectMapper mapper;

    DriverTypeDriverFilter(DriverTypeCache driverTypeCache, BaseActiveDriverSearchCriteria searchCriteria, Map<Long, Integer> grantedTypes) {
      super(searchCriteria);
      this.driverTypeCache = driverTypeCache;
      this.grantedTypes = grantedTypes;
      this.mapper = new ObjectMapper();
    }

    @Override
    public boolean filter(OnlineDriverDto activeDriver) {
      final int requestedBitmask = safeZero(searchCriteria.getDriverTypeBitmask());
      final Map<Boolean, List<CityDriverType>> partition = driverTypeCache.getByCityAndBitmask(activeDriver.getCityId(), activeDriver.getAvailableDriverTypesBitmask())
        .stream()
        .collect(Collectors.partitioningBy(cdt -> cdt.getConfigurationObject(mapper).isExclusive()));
      final List<CityDriverType> assignedExclusiveTypes = partition.getOrDefault(true, new ArrayList<>());
      final List<CityDriverType> assignedNonExclusiveTypes = partition.getOrDefault(false, new ArrayList<>());
      final int declaredBitmask;
      if (!assignedExclusiveTypes.isEmpty()) {
        declaredBitmask = assignedExclusiveTypes
          .stream()
          .mapToInt(CityDriverType::getBitmask)
          .sum();
      } else {
        declaredBitmask = 0;
      }
      final int grantedBitmask = grantedTypes.getOrDefault(activeDriver.getId(), 0)
        | assignedNonExclusiveTypes.stream().mapToInt(CityDriverType::getBitmask).sum();
      final boolean requestedIsGranted = (requestedBitmask & grantedBitmask) == requestedBitmask;
      final boolean driverHasNotDeclared = declaredBitmask == 0;
      final boolean requestedIsDeclared = (requestedBitmask & declaredBitmask) == declaredBitmask;
      return requestedIsGranted && (driverHasNotDeclared || requestedIsDeclared);
    }
  }

  @Slf4j
  static class CarTypeDriverFilter extends DriverFilter<ActiveDriverSearchCriteria> {

    CarTypeDriverFilter(ActiveDriverSearchCriteria searchCriteria) {
      super(searchCriteria);
    }

    @Override
    public boolean filter(OnlineDriverDto activeDriver) {
      int carTypeBitmask = searchCriteria.getCarCategoryBitmask();
      return (activeDriver.getAvailableCarCategoriesBitmask() & carTypeBitmask) != 0;
    }
  }

  static class CompositeFilter<T extends BaseActiveDriverSearchCriteria> extends DriverFilter<T> {

    private final DriverFilter[] filters;

    CompositeFilter(DriverFilter... filters) {
      super(null);
      this.filters = filters;
    }

    @Override
    public boolean filter(OnlineDriverDto activeDriver) {
      for (DriverFilter filter : filters) {
        boolean result = filter.filter(activeDriver);
        if (!result) {
          return false;
        }
      }
      return true;
    }
  }
}
