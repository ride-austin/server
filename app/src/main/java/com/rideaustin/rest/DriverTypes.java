package com.rideaustin.rest;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.rideaustin.CheckedTransactional;
import com.rideaustin.MobileClientEndpoint;
import com.rideaustin.assemblers.CityDriverTypeDtoAssembler;
import com.rideaustin.model.ride.CityDriverType;
import com.rideaustin.rest.model.CityDriverTypeDto;
import com.rideaustin.service.ride.DriverTypeService;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;

@RestController
@CheckedTransactional
@RequestMapping("/rest/driverTypes")
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class DriverTypes {

  private final DriverTypeService driverTypeService;
  private final CityDriverTypeDtoAssembler cityDriverTypeDtoAssembler;

  @GetMapping
  @MobileClientEndpoint
  @ApiOperation("Get available driver types")
  public List<CityDriverTypeDto> listActiveDriverTypes(@ApiParam(value = "City ID", example = "1") @RequestParam(required = false, defaultValue = "1") Long cityId) {
    Collection<CityDriverType> driverTypes = driverTypeService.getCityDriverTypes(cityId);
    if (CollectionUtils.isNotEmpty(driverTypes)) {
      return cityDriverTypeDtoAssembler.toDto(driverTypes);
    }
    return Collections.emptyList();
  }
}
