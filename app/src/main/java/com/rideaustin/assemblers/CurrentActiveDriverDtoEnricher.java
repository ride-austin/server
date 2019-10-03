package com.rideaustin.assemblers;

import javax.inject.Inject;

import org.springframework.stereotype.Component;

import com.rideaustin.rest.model.CurrentActiveDriverDto;
import com.rideaustin.service.location.ObjectLocationService;
import com.rideaustin.service.location.enums.LocationType;
import com.rideaustin.service.model.OnlineDriverDto;
import com.rideaustin.service.user.CarTypesCache;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class CurrentActiveDriverDtoEnricher implements DTOEnricher<CurrentActiveDriverDto> {

  private final ObjectLocationService<OnlineDriverDto> objectLocationService;
  private final CarTypesCache carTypesCache;

  @Override
  public CurrentActiveDriverDto enrich(CurrentActiveDriverDto source) {
    if (source == null) {
      return null;
    }
    OnlineDriverDto driver = objectLocationService.getById(source.getId(), LocationType.ACTIVE_DRIVER);
    if (driver != null) {
      source.setLocation(driver.getLocationObject());
    }
    source.setCarCategories(carTypesCache.fromBitMask(source.getAvailableCarCategories()));
    return source;
  }
}
