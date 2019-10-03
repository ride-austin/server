package com.rideaustin.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import com.rideaustin.model.ride.DriverTypeSearchHandler;
import com.rideaustin.repo.dsl.ActiveDriverDslRepository;
import com.rideaustin.service.location.ObjectLocationService;
import com.rideaustin.service.location.enums.LocationType;
import com.rideaustin.service.model.OnlineDriverDto;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class DirectConnectSearchDriverHandler implements DriverTypeSearchHandler {

  private final ActiveDriverDslRepository activeDriverDslRepository;
  private final ObjectLocationService<OnlineDriverDto> objectLocationService;

  @Override
  public List<OnlineDriverDto> searchDrivers(ActiveDriverSearchCriteria searchCriteria) {
    return doSearch(searchCriteria);
  }

  @Override
  public List<OnlineDriverDto> searchDrivers(QueuedActiveDriverSearchCriteria searchCriteria) {
    return doSearch(searchCriteria);
  }

  private List<OnlineDriverDto> doSearch(BaseActiveDriverSearchCriteria searchCriteria) {
    String directConnectId = (String) searchCriteria.getExtraParams().get("directConnectId");
    if (StringUtils.isEmpty(directConnectId)) {
      return new ArrayList<>();
    }
    OnlineDriverDto activeDriver = activeDriverDslRepository.findByDirectConnectId(directConnectId, searchCriteria.getDriverTypeBitmask());
    if (activeDriver == null || (searchCriteria.getIgnoreIds() != null && searchCriteria.getIgnoreIds().contains(activeDriver.getId()))) {
      return new ArrayList<>();
    }
    Optional<OnlineDriverDto> optionalOnlineDriver = Optional
      .ofNullable(objectLocationService.getById(activeDriver.getId(), LocationType.ACTIVE_DRIVER));
    if (optionalOnlineDriver.isPresent()) {
      activeDriver.setLocationObject(optionalOnlineDriver.get().getLocationObject());
      activeDriver.setAvailableDriverTypesBitmask(optionalOnlineDriver.get().getAvailableDriverTypesBitmask());
      activeDriver.setAvailableCarCategoriesBitmask(optionalOnlineDriver.get().getAvailableCarCategoriesBitmask());
    } else {
      return new ArrayList<>();
    }
    return new ArrayList<>(Collections.singletonList(activeDriver));
  }
}
