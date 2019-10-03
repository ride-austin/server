package com.rideaustin.rest;

import java.util.List;

import javax.inject.Inject;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rideaustin.WebClientEndpoint;
import com.rideaustin.model.City;
import com.rideaustin.service.CityService;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/rest/cities")
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class Cities {

  private final CityService cityService;

  @GetMapping
  @WebClientEndpoint
  @ApiOperation("Get information about all supported cities")
  public List<City> findAll() {
    return cityService.findAll();
  }

  @GetMapping("/{id}")
  @ApiOperation("Get information about requested city")
  public City findById(@ApiParam(value = "City ID", example = "1") @PathVariable("id") long id) {
    return cityService.getById(id);
  }

}