package com.rideaustin.assemblers;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import javax.inject.Inject;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rideaustin.model.ride.CityDriverType;
import com.rideaustin.rest.model.CityDriverTypeDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class CityDriverTypeDtoAssembler implements SingleSideAssembler<CityDriverType, CityDriverTypeDto> {

  private final ObjectMapper mapper;

  @Override
  public CityDriverTypeDto toDto(CityDriverType cityDriverType) {
    if (cityDriverType == null) {
      return null;
    }
    CityDriverType.Configuration configuration = cityDriverType.getConfigurationObject(mapper, cityDriverType.getConfigurationClass());
    Set<String> categories = Optional.ofNullable(cityDriverType.getAvailableInCategories()).orElse(Collections.emptySet());
    configuration.setEligibleCategories(categories);
    return CityDriverTypeDto.builder()
      .cityId(cityDriverType.getCityId())
      .configuration(configuration)
      .description(cityDriverType.getDriverType().getDescription())
      .name(cityDriverType.getDriverType().getName())
      .availableInCategories(categories)
      .build();
  }
}
