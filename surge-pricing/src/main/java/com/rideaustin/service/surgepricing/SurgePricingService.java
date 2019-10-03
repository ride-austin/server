package com.rideaustin.service.surgepricing;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.ImmutableList;
import com.google.maps.model.LatLng;
import com.rideaustin.Constants;
import com.rideaustin.model.enums.RideStatus;
import com.rideaustin.model.redis.RedisSurgeArea;
import com.rideaustin.model.ride.CarType;
import com.rideaustin.model.surgepricing.SurgeArea;
import com.rideaustin.model.surgepricing.SurgeFactor;
import com.rideaustin.repo.dsl.SurgeAreaDslRepository;
import com.rideaustin.repo.redis.SurgeAreaRedisRepository;
import com.rideaustin.rest.exception.BadRequestException;
import com.rideaustin.rest.exception.ConflictException;
import com.rideaustin.rest.exception.NotFoundException;
import com.rideaustin.rest.exception.RideAustinException;
import com.rideaustin.rest.model.PagingParams;
import com.rideaustin.rest.surgepricing.ListSurgeAreaParams;
import com.rideaustin.utils.GeometryUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Inject))
@Slf4j
public class SurgePricingService {

  private static final String SURGE_AREA_NOT_FOUND_MESSAGE = "Surge area not found";
  static final Set<RideStatus> REQUESTED_RIDE_STATUSES = EnumSet.of(RideStatus.NO_AVAILABLE_DRIVER, RideStatus.REQUESTED);
  static final Set<RideStatus> ACCEPTED_RIDE_STATUSES = EnumSet.of(RideStatus.ACTIVE, RideStatus.DRIVER_REACHED);

  private final SurgeAreaDslRepository surgeAreaDslRepository;
  private final SurgeAreaRedisRepository surgeAreaRedisRepository;
  private final SurgeAreaHistoryService surgeAreaHistoryService;
  private final SurgePricingNotificationService notificationService;

  public Page<RedisSurgeArea> listSurgeAreas(ListSurgeAreaParams searchCriteria, PagingParams paging) {
    Page<RedisSurgeArea> surgeAreas;
    if (searchCriteria.getLatitude() != null && searchCriteria.getLongitude() != null) {
      List<RedisSurgeArea> contents = findByCoordinates(searchCriteria.getLatitude(), searchCriteria.getLongitude(),
        searchCriteria.getCityId());
      surgeAreas = new PageImpl<>(contents, paging.toPageRequest(), contents.size());
    } else {
      List<RedisSurgeArea> areas = ImmutableList.copyOf(surgeAreaRedisRepository.findByCityId(searchCriteria.getCityId()))
        .stream()
        .filter(searchCriteria.filter())
        .collect(Collectors.toList());
      surgeAreas = new PageImpl<>(areas, paging.toPageRequest(), areas.size());
    }
    return surgeAreas;
  }

  @Transactional
  public SurgeArea updateSurgeArea(Long surgeAreaId, SurgeArea updatedSurgeArea, boolean forceNotify) throws RideAustinException {
    SurgeArea surgeArea = surgeAreaDslRepository.findOne(surgeAreaId);
    return updateSurgeArea(surgeArea, updatedSurgeArea, forceNotify);
  }

  private SurgeArea updateSurgeArea(SurgeArea surgeArea, SurgeArea updatedSurgeArea, boolean forceNotify) throws RideAustinException {
    validateSurgeAreaGeometry(updatedSurgeArea);
    if (surgeArea == null) {
      throw new NotFoundException(SURGE_AREA_NOT_FOUND_MESSAGE);
    }
    if (!surgeArea.getName().equals(updatedSurgeArea.getName())) {
      validateNameAlreadyExists(updatedSurgeArea);
    }
    boolean shouldSendNotification = shouldSendNotification(surgeArea, updatedSurgeArea);
    populateSurgeAreaAdminData(surgeArea, updatedSurgeArea);
    surgeArea = surgeAreaDslRepository.save(surgeArea);
    updateSurgeAreaCache(surgeArea);
    if (shouldSendNotification && forceNotify) {
      notificationService.notifyUsers(updatedSurgeArea);
    }
    return surgeArea;
  }

  public Optional<RedisSurgeArea> getSurgeAreaByCarType(double latitude, double longitude, Long cityId, CarType carType) {
    return selectSurgeAreaByFactor(findByCoordinates(latitude, longitude, cityId), carType);
  }

  public BigDecimal getSurgeFactor(LatLng startLocation, CarType carType, Long cityId) {
    Optional<RedisSurgeArea> surgeArea = getSurgeAreaByCarType(startLocation.lat, startLocation.lng, cityId, carType);
    return surgeArea
      .filter(rsa -> rsa.supports(carType))
      .map(rsa -> rsa.getSurgeFactor(carType))
      .orElse(Constants.NEUTRAL_SURGE_FACTOR);
  }

  @Transactional
  public SurgeArea createSurgeArea(SurgeArea surgeArea) throws RideAustinException {
    validateSurgeAreaGeometry(surgeArea);
    validateNameAlreadyExists(surgeArea);
    SurgeArea savedSurgeArea = surgeAreaDslRepository.save(surgeArea);
    notificationService.notifyUsers(savedSurgeArea);
    return savedSurgeArea;
  }

  @Transactional
  public void removeSurgeArea(Long surgeAreaId) throws NotFoundException {
    SurgeArea surgeArea = Optional.ofNullable(surgeAreaDslRepository.findOne(surgeAreaId)).orElseThrow(() -> new NotFoundException(SURGE_AREA_NOT_FOUND_MESSAGE));
    surgeArea.setActive(false);
    surgeAreaDslRepository.save(surgeArea);
  }

  @Transactional
  public List<String> updateSurgeAreas(List<SurgeArea> surgeAreas) {
    List<String> failToUpdate = new ArrayList<>();
    List<SurgeArea> toNotify = new ArrayList<>();
    surgeAreas.forEach(surgeArea -> {
      try {
        SurgeArea old = surgeAreaDslRepository.findOne(surgeArea.getId());
        boolean shouldNotify = shouldSendNotification(old, surgeArea);
        SurgeArea updated = updateSurgeArea(old, surgeArea, false);
        if (shouldNotify) {
          toNotify.add(updated);
        }
      } catch (RideAustinException e) {
        log.error("Error updating surge area:" + surgeArea.getId(), e);
        failToUpdate.add(surgeArea.getName());
      }
    });
    notificationService.notifyUsers(toNotify);
    return failToUpdate;
  }

  public boolean isSurgeMandatory(Double latitude, Double longitude, CarType carType, Long cityId) {
    Optional<RedisSurgeArea> surgeArea = getSurgeAreaByCarType(latitude, longitude, cityId, carType);
    return surgeArea.map(rsa -> rsa.isMandatory(carType)).orElse(false);
  }

  List<RedisSurgeArea> findByCoordinates(double latitude, double longitude, List<RedisSurgeArea> areas) {
    return areas
      .stream()
      .filter(surgeArea -> GeometryUtils.buildPolygon(surgeArea.getAreaGeometry().getCsvGeometry()).contains(latitude, longitude))
      .collect(Collectors.toList());
  }

  Optional<RedisSurgeArea> selectSurgeAreaByFactor(List<RedisSurgeArea> surgeAreas, CarType carType) {
    RedisSurgeArea maxSFArea = null;
    for (RedisSurgeArea surgeArea : surgeAreas) {
      if (carType != null && surgeArea.supports(carType) &&
        (maxSFArea == null || (maxSFArea.getSurgeFactor(carType).compareTo(surgeArea.getSurgeFactor(carType)) < 0))) {
        maxSFArea = surgeArea;
      }
    }
    return Optional.ofNullable(maxSFArea);
  }

  private void updateSurgeAreaCache(SurgeArea area) {
    surgeAreaHistoryService.persistHistoryItem(area);
    if (area.isActive()) {
      RedisSurgeArea redisSurgeArea = surgeAreaRedisRepository.findOne(area.getId());
      redisSurgeArea.updateFieldsFrom(area);
      redisSurgeArea.setSurgeMapping(new HashMap<>());
      area.getSurgeFactors().forEach(sf -> redisSurgeArea.getSurgeMapping().put(sf.getCarType(), sf.getValue()));
      surgeAreaRedisRepository.save(redisSurgeArea);
    } else {
      surgeAreaRedisRepository.delete(area.getId());
    }
  }

  private List<RedisSurgeArea> findByCoordinates(double latitude, double longitude, Long cityId) {
    return findByCoordinates(latitude, longitude, surgeAreaRedisRepository.findByCityId(cityId));
  }

  private void validateNameAlreadyExists(SurgeArea surgeArea) throws ConflictException {
    if (surgeAreaDslRepository.findByAreaName(surgeArea.getName()) != null) {
      throw new ConflictException(String.format("Surge area with name %s already exists", surgeArea.getName()));
    }
  }

  private void validateSurgeAreaGeometry(SurgeArea updatedSurgeArea) throws BadRequestException {
    try {
      GeometryUtils.buildPolygon(updatedSurgeArea.getAreaGeometry().getCsvGeometry());
    } catch (Exception e) {
      log.error("Failed to build polygon", e);
      throw new BadRequestException(e.getMessage());
    }
  }

  private boolean safeNotEquals(Set<SurgeFactor> o1, Set<SurgeFactor> o2) {
    boolean firstCheck = o1 != null && o2 != null && o1.size() == o2.size();
    if (!firstCheck) {
      return false;
    }
    Comparator<SurgeFactor> comparator = Comparator.comparing(SurgeFactor::getCarType);
    List<SurgeFactor> list1 = o1.stream().sorted(comparator).collect(Collectors.toList());
    List<SurgeFactor> list2 = o2.stream().sorted(comparator).collect(Collectors.toList());
    for (int i = 0; i < list1.size(); i++) {
      if (safeNotEquals(list1.get(i), list2.get(i))) {
        return true;
      }
    }
    return false;
  }

  private <T> boolean safeNotEquals(Comparable<T> o1, T o2) {
    return o1 != null && o2 != null && o1.compareTo(o2) != 0;
  }

  private boolean shouldSendNotification(SurgeArea existing, SurgeArea updatedSurgeArea) {
    return existing == null ||
      safeNotEquals(updatedSurgeArea.getSurgeFactors(), existing.getSurgeFactors()) ||
      existing.getCarCategoriesBitmask() != updatedSurgeArea.getCarCategoriesBitmask();
  }

  private void populateSurgeAreaAdminData(SurgeArea existing, SurgeArea updated) {
    Map<String, SurgeFactor> existingSurgeFactors = existing.getSurgeFactors().stream().collect(Collectors.toMap(SurgeFactor::getCarType, Function.identity()));
    Set<SurgeFactor> newSurgeFactors = mergeSurgeFactors(existingSurgeFactors, updated.getSurgeFactors());
    existing.getSurgeFactors().clear();
    existing.getSurgeFactors().addAll(newSurgeFactors);
    existing.setCarCategoriesBitmask(updated.getCarCategoriesBitmask());
    existing.setName(updated.getName());
    existing.setAutomated(updated.isAutomated());
    existing.getAreaGeometry().setCsvGeometry(updated.getAreaGeometry().getCsvGeometry());
    existing.getAreaGeometry().setLabelLat(updated.getAreaGeometry().getLabelLat());
    existing.getAreaGeometry().setLabelLng(updated.getAreaGeometry().getLabelLng());
    existing.getAreaGeometry().setTopLeftCornerLat(updated.getAreaGeometry().getTopLeftCornerLat());
    existing.getAreaGeometry().setTopLeftCornerLng(updated.getAreaGeometry().getTopLeftCornerLng());
    existing.getAreaGeometry().setBottomRightCornerLat(updated.getAreaGeometry().getBottomRightCornerLat());
    existing.getAreaGeometry().setBottomRightCornerLng(updated.getAreaGeometry().getBottomRightCornerLng());
    existing.getAreaGeometry().setCenterPointLat(updated.getAreaGeometry().getCenterPointLat());
    existing.getAreaGeometry().setCenterPointLng(updated.getAreaGeometry().getCenterPointLng());
  }

  private Set<SurgeFactor> mergeSurgeFactors(Map<String, SurgeFactor> existingSurgeFactors, Set<SurgeFactor> newSurgeFactors) {
    return newSurgeFactors.stream().peek(nsf -> {
      SurgeFactor surgeFactor = existingSurgeFactors.get(nsf.getCarType());
      if (surgeFactor != null) {
        nsf.setId(surgeFactor.getId());
      }
    }).collect(Collectors.toSet());
  }

}
