package com.rideaustin.service.areaqueue;

import static java.util.stream.Collectors.reducing;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Lists;
import com.rideaustin.model.Area;
import com.rideaustin.model.AreaQueueEntry;
import com.rideaustin.model.ride.ActiveDriver;
import com.rideaustin.model.user.Driver;
import com.rideaustin.repo.dsl.AreaQueueEntryDslRepository;
import com.rideaustin.rest.exception.NotFoundException;
import com.rideaustin.rest.model.AreaQueueInfo;
import com.rideaustin.rest.model.AreaQueuePositions;
import com.rideaustin.service.ActiveDriverLocationService;
import com.rideaustin.service.ActiveDriversService;
import com.rideaustin.service.location.enums.LocationType;
import com.rideaustin.service.model.OnlineDriverDto;
import com.rideaustin.service.user.CarTypesCache;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class AreaQueueService {

  private final AreaService areaService;
  private final AreaQueueEntryService areaQueueEntryService;
  private final ActiveDriversService activeDriversService;
  private final ActiveDriverLocationService activeDriverLocationService;

  private final AreaQueueEntryDslRepository areaQueueEntryDslRepository;

  private final CarTypesCache carTypesCache;

  public List<AreaQueuePositions> getActiveAreaDetails(Long cityId) {
    List<AreaQueuePositions> areasDetails = Lists.newArrayList();
    List<Area> areas = areaService.getAreasPerCity(cityId);
    if (areas != null) {
      for (Area area : areas) {
        if (!area.isVisibleToDrivers()) {
          continue;
        }
        AreaQueuePositions areaDetails = new AreaQueuePositions(area, carTypesCache.getActiveCarTypes()
          .keySet().stream().collect(Collectors.toMap(i -> i, i -> 0)));

        areaDetails.getLengths().putAll(areaQueueEntryService.getEntries(area).stream()
          .collect(Collectors.groupingBy(AreaQueueEntry::getCarCategory, reducing(0, e -> 1, Integer::sum))));

        areasDetails.add(areaDetails);
      }
    }
    return areasDetails;
  }

  public AreaQueueInfo getPositions(Long areaId) throws NotFoundException {
    Area area = areaService.getById(areaId);
    if (area == null) {
      throw new NotFoundException("Area not found");
    }
    List<AreaQueueEntry> entries = areaQueueEntryService.getEntries(area);
    return new AreaQueueInfo(entries.stream().collect(Collectors.groupingBy(AreaQueueEntry::getCarCategory)));
  }

  public AreaQueuePositions calculateDriverCurrentPositionInQueue(Driver driver) {
    AreaQueuePositions returnValue = new AreaQueuePositions();
    ActiveDriver activeDriver = activeDriversService.getActiveDriverByDriver(driver.getUser());
    if (activeDriver == null) {
      return returnValue;
    }
    OnlineDriverDto onlineDriverDto = activeDriverLocationService.getById(activeDriver.getId(), LocationType.ACTIVE_DRIVER);
    Area currentArea = getCurrentActiveDriverArea(activeDriver);

    if (currentArea == null) {
      return returnValue;
    }
    Set<String> carCategories = carTypesCache.fromBitMask(onlineDriverDto.getAvailableCarCategoriesBitmask());

    for (String category : carCategories) {
      Pair<Integer, Integer> queueData = calculateActiveDriverPositionInQueueByCarCategory(currentArea, activeDriver.getId(), category);
      returnValue.getLengths().put(category, queueData.getRight());
      if (queueData.getLeft() > -1) {
        returnValue.getPositions().put(category, queueData.getLeft());
      }
    }
    returnValue.setAreaQueueName(currentArea.getName());
    return returnValue.withDisplayConfigFor(currentArea);
  }

  private Pair<Integer, Integer> calculateActiveDriverPositionInQueueByCarCategory(Area area, long activeDriverId, String carCategory) {
    List<Long> activeDriverIdsByCarCategory = areaQueueEntryDslRepository.findActiveDriverIdsByCarCategory(area, carTypesCache.getCarType(carCategory));
    if (CollectionUtils.isEmpty(activeDriverIdsByCarCategory)) {
      return new ImmutablePair<>(-1, activeDriverIdsByCarCategory.size());
    }
    for (int i = 0; i < activeDriverIdsByCarCategory.size(); i++) {
      if (Objects.equals(activeDriverIdsByCarCategory.get(i), activeDriverId)) {
        return new ImmutablePair<>(i, activeDriverIdsByCarCategory.size());
      }
    }
    return new ImmutablePair<>(-1, activeDriverIdsByCarCategory.size());
  }

  public Area getCurrentActiveDriverArea(@Nonnull ActiveDriver activeDriver) {
    List<AreaQueueEntry> entries = areaQueueEntryService.getCurrentActiveDriverAreaQueueEntry(activeDriver.getId());
    if (CollectionUtils.isEmpty(entries)) {
      return null;
    }
    return entries.get(0).getArea();
  }

}
