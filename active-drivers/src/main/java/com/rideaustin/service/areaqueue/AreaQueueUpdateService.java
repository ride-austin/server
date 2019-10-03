package com.rideaustin.service.areaqueue;

import java.util.ArrayList;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.ImmutableMap;
import com.rideaustin.model.Area;
import com.rideaustin.model.AreaQueueEntry;
import com.rideaustin.model.enums.ActiveDriverStatus;
import com.rideaustin.model.ride.ActiveDriver;
import com.rideaustin.model.ride.Ride;
import com.rideaustin.repo.dsl.ActiveDriverDslRepository;
import com.rideaustin.repo.dsl.AreaDslRepository;
import com.rideaustin.repo.dsl.AreaQueueEntryDslRepository;
import com.rideaustin.repo.dsl.RideDslRepository;
import com.rideaustin.rest.exception.RideAustinException;
import com.rideaustin.rest.exception.ServerError;
import com.rideaustin.service.ActiveDriverLocationService;
import com.rideaustin.service.config.AreaQueueConfig;
import com.rideaustin.service.event.EventsNotificationService;
import com.rideaustin.service.generic.TimeService;
import com.rideaustin.service.location.enums.LocationType;
import com.rideaustin.service.model.ConsecutiveDeclinedRequestsData;
import com.rideaustin.service.model.OnlineDriverDto;
import com.rideaustin.service.user.CarTypesCache;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class AreaQueueUpdateService {

  private static final String AREA_QUEUE_NAME = "areaQueueName";

  private final AreaService areaService;
  private final AreaQueueEntryService areaQueueEntryService;
  private final EventsNotificationService notificationService;
  private final TimeService timeService;
  private final ActiveDriverLocationService activeDriverLocationService;
  private final AreaQueuePenaltyService penaltyService;

  private final ActiveDriverDslRepository activeDriverDslRepository;
  private final RideDslRepository rideDslRepository;
  private final AreaDslRepository areaDslRepository;
  private final AreaQueueEntryDslRepository areaQueueEntryDslRepository;

  private final CarTypesCache carTypesCache;
  private final AreaQueueConfig config;
  private final ApplicationEventPublisher publisher;

  /**
   * Check if every driver from area should still by inside that area.
   * Add to queue AD that just entered
   * This should clean area queue from wrong ActiveDrivers and add new ones
   * Should by executed from Quartz JOB.
   */
  public void updateStatuses(Long areaId) throws RideAustinException {
    log.debug("updating area statuses for area: " + areaId);
    Area area = areaService.getById(areaId);
    List<AreaQueueEntry> entries = areaQueueEntryService.getEntries(area);
    List<Long> beginQueuePositionCache = extractEntriesActiveDriversIndex(entries);
    List<Long> changeablePositionCache = extractEntriesActiveDriversIndex(entries);
    List<OnlineDriverDto> activeDriversInsideArea = getActiveDrivers(area, EnumSet.of(ActiveDriverStatus.AVAILABLE, ActiveDriverStatus.AWAY));
    log.debug("activeDriversInsideArea:" + activeDriversInsideArea.size());
    removeLeavingActiveDrivers(entries, changeablePositionCache);
    updateEnteringActiveDrivers(area);
    updateDriversChangeCarCategories(area, activeDriversInsideArea);
    sendPositionUpdateNotifications(area, beginQueuePositionCache, changeablePositionCache);
  }

  private List<Long> extractEntriesActiveDriversIndex(List<AreaQueueEntry> entries) {
    List<Long> returnValue = new ArrayList<>();
    for (AreaQueueEntry entry : entries) {
      if (!returnValue.contains(entry.getActiveDriver().getId())) {
        returnValue.add(entry.getActiveDriver().getId());
      }
    }
    return returnValue;
  }

  private List<OnlineDriverDto> getActiveDrivers(Area area, Set<ActiveDriverStatus> approvedStatus) {
    return activeDriverLocationService.searchActiveInsideArea(area, approvedStatus);
  }

  private void removeLeavingActiveDrivers(List<AreaQueueEntry> entries, List<Long> changeablePositionCache) throws RideAustinException {
    List<Long> removedIs = new ArrayList<>();
    for (AreaQueueEntry entry : entries) {
      Area currentArea = areaService.isInArea(entry.getActiveDriver(), entry.getActiveDriver().getCityId());
      if (removedIs.contains(entry.getActiveDriver().getId())
        || removeDriversInRide(changeablePositionCache, removedIs, entry)
        || removeInactiveDrivers(changeablePositionCache, removedIs, entry)
        || removeLeftDrivers(changeablePositionCache, removedIs, entry, currentArea)
        || removePenalizedDrivers(changeablePositionCache, removedIs, entry)) {
        continue;
      }
      removeDriversChangedArea(changeablePositionCache, removedIs, entry, currentArea);
    }
  }

  private void checkAndAddNewEntriesCarTypes(Area area, OnlineDriverDto ad, Set<String> carCategories, List<AreaQueueEntry> entryList) {
    for (String cc : carCategories) {
      if (checkIsEntriesListContainsCarCategory(cc, entryList) == null && !penaltyService.isPenalized(ad.getDriverId())) {
        log.debug("registering new car  category: {} entry for active driver {} into area {} :  ", cc, ad.getId(), area.getName());
        registerNewAreaEntry(area, ad, cc);
      }
    }
  }

  private AreaQueueEntry checkIsEntriesListContainsCarCategory(String cc, List<AreaQueueEntry> entryList) {
    for (AreaQueueEntry entry : entryList) {
      if (entry.getCarCategory().equals(cc)) {
        return entry;
      }
    }
    return null;
  }

  private boolean removeDriversInRide(List<Long> changeablePositionCache, List<Long> removedIs, AreaQueueEntry entry) throws RideAustinException {
    final Ride activeRide = rideDslRepository.findActiveByActiveDriver(entry.getActiveDriver());
    boolean hasActiveRide = null != activeRide;
    if (hasActiveRide) {
      log.info(String.format("[QUEUE_REMOVE][AD %d] Driver has active ride %d", entry.getActiveDriver().getId(), activeRide.getId()));
      performRemovingFromQueue(changeablePositionCache, removedIs, entry);
      notificationService.sendQueuedAreaTakingRideToDriver(entry.getActiveDriver().getDriver().getId(), getAreaParameters(entry.getArea()));
      return true;
    }
    return false;
  }

  private void performRemovingFromQueue(List<Long> changeablePositionCache, List<Long> removedIs, AreaQueueEntry entry) {
    removeFromAreaQueue(entry);
    changeablePositionCache.remove(entry.getActiveDriver().getId());
    removedIs.add(entry.getActiveDriver().getId());
  }

  private Map<String, String> getAreaParameters(Area area) {
    String name = area.getName();
    if (!area.isVisibleToDrivers() && area.getParentAreaId() != null) {
      name = areaService.getById(area.getParentAreaId()).getName();
    }
    return ImmutableMap.of(AREA_QUEUE_NAME, name);
  }

  private boolean removeInactiveDrivers(List<Long> changeablePositionCache, List<Long> removedIs, AreaQueueEntry entry) throws ServerError {
    boolean driverIsInactive = ActiveDriverStatus.INACTIVE.equals(entry.getActiveDriver().getStatus());
    if (driverIsInactive) {
      if (!isDateInLimit(entry.getActiveDriver().getInactiveOn(), config.getInactiveTimeThresholdBeforeLeave())) {
        log.info(String.format("[QUEUE_REMOVE][AD %d] Driver is inactive", entry.getActiveDriver().getId()));
        performRemovingFromQueue(changeablePositionCache, removedIs, entry);
        notificationService.sendQueuedAreaGoingInactiveToDriver(entry.getActiveDriver().getDriver().getId(), getAreaParameters(entry.getArea()));
      }
      return true;
    }
    return false;
  }

  private void sendPositionUpdateNotifications(Area area, List<Long> beginQueuePositionCache, List<Long> changeablePositionCache)
    throws RideAustinException {
    if (!area.isVisibleToDrivers()) {
      return;
    }
    Map<String, String> areaParams = getAreaParameters(area);

    for (Long activeDriverId : changeablePositionCache) {
      if ((beginQueuePositionCache.indexOf(activeDriverId) != -1) && (beginQueuePositionCache.indexOf(activeDriverId) != changeablePositionCache.indexOf(activeDriverId))) {
        ActiveDriver ad = activeDriverDslRepository.findById(activeDriverId);
        notificationService.sendQueuedAreaUpdateToDriver(ad.getDriver().getId(), areaParams);
      }
    }
  }

  private boolean isDateInLimit(Date date, int limit) {
    return (DateUtils.addMinutes(date, limit)).after(timeService.getCurrentDate());
  }

  private boolean removeLeftDrivers(List<Long> changeablePositionCache, List<Long> removedIs, AreaQueueEntry entry, Area area) throws ServerError {
    boolean isInExclusion = areaService.isInExclusion(entry.getActiveDriver());
    int leavingThreshold = config.getOutOfAreaTimeThresholdBeforeLeave();
    if (isInExclusion) {
      leavingThreshold = config.getExclusionTimeThresholdBeforeLeave();
    }
    if (area == null) {
      if (entry.getLastPresentInQueue() != null &&
        !isDateInLimit(entry.getLastPresentInQueue(), leavingThreshold)) {
        log.info(String.format("[QUEUE_REMOVE][AD %d] Driver is out of queue zone (%s,%s)",
          entry.getActiveDriver().getId(), entry.getActiveDriver().getLatitude(),
          entry.getActiveDriver().getLongitude()));
        performRemovingFromQueue(changeablePositionCache, removedIs, entry);
        notificationService.sendQueuedAreaLeavingToDriver(entry.getActiveDriver().getDriver().getId(), getAreaParameters(entry.getArea()));
        return true;
      }
    } else {
      entry.setLastPresentInQueue(timeService.getCurrentDate());
    }
    return false;
  }

  private boolean removePenalizedDrivers(List<Long> changeablePositionCache, List<Long> removedIs, AreaQueueEntry entry) throws ServerError {
    if (penaltyService.isPenalized(entry.getActiveDriver().getDriver().getId())) {
      final OnlineDriverDto onlineDriver = activeDriverLocationService.getById(entry.getActiveDriver().getId(), LocationType.ACTIVE_DRIVER);
      if (onlineDriver != null) {
        final ConsecutiveDeclinedRequestsData consecutiveDeclinedRequests = onlineDriver.getConsecutiveDeclinedRequests();
        for (Map.Entry<String, Integer> cdrEntry : consecutiveDeclinedRequests.entrySet()) {
          if (Objects.equals(cdrEntry.getValue(), config.getMaxDeclines())) {
            consecutiveDeclinedRequests.reset(cdrEntry.getKey());
            break;
          }
        }
        onlineDriver.setConsecutiveDeclinedRequests(new ConsecutiveDeclinedRequestsData(consecutiveDeclinedRequests));
        activeDriverLocationService.saveOrUpdateLocationObject(onlineDriver);
      }
      log.info(String.format("[QUEUE_REMOVE][AD %d] Driver is penalized", entry.getActiveDriver().getId()));
      performRemovingFromQueue(changeablePositionCache, removedIs, entry);
      notificationService.sendQueuedAreaPenalizedToDriver(entry.getActiveDriver().getDriver().getId(), getAreaParameters(entry.getArea()));
      return true;
    }
    return false;
  }

  private void removeDriversChangedArea(List<Long> changeablePositionCache, List<Long> removedIs, AreaQueueEntry entry, Area area) throws ServerError {
    if (area != null && area.getId() != entry.getArea().getId() && (area.getParentAreaId() == null || area.getParentAreaId() != entry.getArea().getId())) {
      log.info(String.format("[QUEUE_REMOVE][AD %d] Driver has moved from %s to another area %s", entry.getActiveDriver().getId(),
        entry.getArea().getName(), area.getName()));
      performRemovingFromQueue(changeablePositionCache, removedIs, entry);
      notificationService.sendQueuedAreaLeavingToDriver(entry.getActiveDriver().getDriver().getId(), getAreaParameters(area));
    }
  }

  private void updateEnteringActiveDrivers(Area area) throws RideAustinException {
    for (OnlineDriverDto ad : getActiveDrivers(area, EnumSet.of(ActiveDriverStatus.AVAILABLE))) {
      List<AreaQueueEntry> currentEntry = areaQueueEntryService.getCurrentActiveDriverAreaQueueEntry(ad.getId(), area);

      if (CollectionUtils.isEmpty(currentEntry)) {
        //searching for active entry with inactive driver for this driver
        List<AreaQueueEntry> enableEntriesForInactiveAD = getActiveEntryForInactiveDriver(ad.getDriverId());
        if (CollectionUtils.isNotEmpty(enableEntriesForInactiveAD)) {
          log.debug("entering but has enabled entries for inactive AD  currentEntry for ad. Driver.id: {}, old entries: {} :  ", ad.getId(), enableEntriesForInactiveAD.size());
          for (AreaQueueEntry oldEntry : enableEntriesForInactiveAD) {
            if (isDateInLimit(oldEntry.getActiveDriver().getInactiveOn(), config.getInactiveTimeThresholdBeforeLeave())
              && !penaltyService.isPenalized(ad.getDriverId())) {
              log.debug("Updating old AQ entry for new driver, because inactive is below limit");
              oldEntry.setActiveDriver(activeDriverDslRepository.findById(ad.getId()));
              areaDslRepository.save(oldEntry);
              if (area.isVisibleToDrivers()) {
                notificationService.sendQueuedAreaUpdateToDriver(ad.getDriverId(), getAreaParameters(area));
              }
            }
          }
        } else if (!penaltyService.isPenalized(ad.getDriverId())) {
          log.debug("there is not any entries for ad {}. Will create new one. :  ", ad.getId());
          createAndNotifyNewAreaEntry(area, ad);
        }
      }
    }
  }

  private void createAndNotifyNewAreaEntry(Area area, OnlineDriverDto ad) throws RideAustinException {
    log.debug("Sending notification for new area entry to ad {}", ad.getId());
    registerNewActiveDriverInsideArea(area, ad);
    notificationService.sendQueuedAreaEnteringToDriver(ad.getDriverId(), getAreaParameters(area));
  }

  private List<AreaQueueEntry> getActiveEntryForInactiveDriver(Long driverId) {
    return areaQueueEntryDslRepository.findEnabledForOfflineActiveDriver(driverId);
  }

  private void registerNewActiveDriverInsideArea(@Nonnull Area area, @Nonnull OnlineDriverDto activeDriver) {
    boolean skipProcessing = false;
    Area currentArea = areaService.isInArea(activeDriver, activeDriver.getCityId());
    List<AreaQueueEntry> currentEntry = areaQueueEntryService.getCurrentActiveDriverAreaQueueEntry(activeDriver.getId(), currentArea);
    if (currentArea == null) {
      log.warn("Active driver is not inside any area");
      skipProcessing = true;
    }
    if (currentArea != null && currentArea.getId() != area.getId() && currentArea.getParentAreaId() != area.getId()) {
      log.warn("Active driver is inside another area");
      skipProcessing = true;
    }
    if (CollectionUtils.isNotEmpty(currentEntry)) {
      log.warn("Active driver is already register inside area");
      skipProcessing = true;
    }
    if (skipProcessing) {
      return;
    }

    Set<String> carCategories = carTypesCache.fromBitMask(activeDriver.getAvailableCarCategoriesBitmask());
    for (String category : carCategories) {
      registerNewAreaEntry(area, activeDriver, category);
    }

  }

  private void registerNewAreaEntry(@Nonnull Area area, @Nonnull OnlineDriverDto activeDriver, @Nonnull String carCategory) {
    AreaQueueEntry entry = new AreaQueueEntry();
    entry.setActiveDriver(activeDriverDslRepository.findById(activeDriver.getId()));
    entry.setArea(area);
    entry.setEnabled(true);
    entry.setCarCategory(carCategory);
    entry.setLastPresentInQueue(timeService.getCurrentDate());
    areaQueueEntryDslRepository.save(entry);
  }

  private void removeFromAreaQueue(@Nonnull AreaQueueEntry entry) {
    List<AreaQueueEntry> currentEntries = areaQueueEntryService.getCurrentActiveDriverAreaQueueEntry(entry.getActiveDriver().getId(),
      entry.getArea());
    if (CollectionUtils.isEmpty(currentEntries)) {
      return;
    }
    currentEntries.forEach(e -> e.setEnabled(false));
    areaQueueEntryDslRepository.saveMany(currentEntries);
  }

  private void updateDriversChangeCarCategories(Area area, List<OnlineDriverDto> activeDriversInsideArea) {
    for (OnlineDriverDto ad : activeDriversInsideArea) {
      List<AreaQueueEntry> entryList = areaQueueEntryService.getCurrentActiveDriverAreaQueueEntry(ad.getId());
      Set<String> carCategories = carTypesCache.fromBitMask(ad.getAvailableCarCategoriesBitmask());
      disableEntryFromRemovedCarCategory(area, ad, carCategories, entryList);
      checkAndAddNewEntriesCarTypes(area, ad, carCategories, entryList);
    }
  }

  private void disableEntryFromRemovedCarCategory(Area area, OnlineDriverDto ad, Set<String> carCategories, List<AreaQueueEntry> entryList) {
    for (AreaQueueEntry entry : entryList) {
      if (!carCategories.contains(entry.getCarCategory())) {
        log.debug("removing car category: {} entry for active driver {} and area {} :  ", entry.getCarCategory(), ad.getId(), area.getName());
        entry.setEnabled(false);
        areaDslRepository.save(entry);
      }
    }
  }
}
