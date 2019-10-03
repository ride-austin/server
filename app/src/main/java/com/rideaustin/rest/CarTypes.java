package com.rideaustin.rest;

import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.rideaustin.assemblers.CityCarTypeDtoAssembler;
import com.rideaustin.model.enums.AvatarType;
import com.rideaustin.model.ride.CityCarType;
import com.rideaustin.rest.model.CityCarTypeDto;
import com.rideaustin.service.CarTypeService;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class CarTypes {

  private final CarTypeService carTypeService;
  private final CityCarTypeDtoAssembler cityCarTypeDtoAssembler;

  @GetMapping({"/rest/carTypes", "/rest/drivers/carTypes"})
  @RolesAllowed({AvatarType.ROLE_RIDER, AvatarType.ROLE_DRIVER, AvatarType.ROLE_ADMIN})
  @ApiOperation("Get list of enabled car categories")
  public List<CityCarTypeDto> listActiveCarTypes(
    @ApiParam(value = "City ID", required = true, example = "1")
    @RequestParam(defaultValue = "1") Long cityId
  ) {
    List<CityCarType> cityCarTypes = carTypeService.getCityCarTypes(cityId);
    return cityCarTypeDtoAssembler.toDto(cityCarTypes);
  }

}
