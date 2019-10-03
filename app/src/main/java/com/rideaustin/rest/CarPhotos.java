package com.rideaustin.rest;

import java.util.List;

import javax.inject.Inject;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rideaustin.CheckedTransactional;
import com.rideaustin.DriverEndpoint;
import com.rideaustin.assemblers.CarPhotoDtoAssembler;
import com.rideaustin.rest.model.CarPhotoDto;
import com.rideaustin.service.user.CarPhotoService;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;

@RestController
@CheckedTransactional
@RequestMapping("/rest/carphotos")
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class CarPhotos {

  private final CarPhotoService carPhotoService;
  private final CarPhotoDtoAssembler dtoAssembler;

  @DriverEndpoint
  @GetMapping("/car/{carId}")
  @ApiOperation("List of car photos to get as a driver")
  public List<CarPhotoDto> getPhotos(@ApiParam(value = "Car ID", example = "1") @PathVariable("carId") Long carId) {
    return dtoAssembler.toDto(carPhotoService.getCarPhotos(carId));
  }

}
