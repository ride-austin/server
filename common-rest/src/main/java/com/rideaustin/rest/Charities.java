package com.rideaustin.rest;

import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;

import org.apache.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.rideaustin.RiderEndpoint;
import com.rideaustin.model.Charity;
import com.rideaustin.model.City;
import com.rideaustin.model.enums.AvatarType;
import com.rideaustin.repo.dsl.CharityDslRepository;
import com.rideaustin.rest.exception.BadRequestException;
import com.rideaustin.rest.exception.RideAustinException;
import com.rideaustin.service.CityCache;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class Charities {

  private final CharityDslRepository charityDslRepository;
  private final CityCache cityCache;

  @RiderEndpoint
  @GetMapping("/rest/charities")
  @RolesAllowed({AvatarType.ROLE_ADMIN, AvatarType.ROLE_RIDER})
  @ApiOperation(value = "List all available charity organizations")
  @ApiResponses({
    @ApiResponse(code = HttpStatus.SC_OK, message = "OK"),
    @ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = "City ID is invalid")
  })
  public List<Charity> listCharities(@ApiParam(value = "City ID", required = true, defaultValue = "1", example = "1") @RequestParam(defaultValue = "1") Long cityId) throws RideAustinException {
    City city = cityCache.getCity(cityId);
    if (city == null) {
      throw new BadRequestException("Invalid city id");
    }

    return charityDslRepository.findAllByCity(city);
  }

}
