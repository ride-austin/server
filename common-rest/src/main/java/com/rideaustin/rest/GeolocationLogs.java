package com.rideaustin.rest;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;

import org.apache.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.rideaustin.model.enums.AvatarType;
import com.rideaustin.model.enums.GeolocationLogEvent;
import com.rideaustin.rest.exception.ForbiddenException;
import com.rideaustin.rest.model.GeolocationLogDto;
import com.rideaustin.service.GeolocationLogService;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class GeolocationLogs {

  private final GeolocationLogService geolocationLogService;

  @RolesAllowed(AvatarType.ROLE_RIDER)
  @ApiOperation("Store rider's position to determine higher demand areas")
  @PostMapping(path = "/rest/geolog", produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponses({
    @ApiResponse(code = HttpStatus.SC_OK, message = "OK"),
    @ApiResponse(code = HttpStatus.SC_FORBIDDEN, message = "Requesting user is trying to add location for some other user")
  })
  public GeolocationLogDto storeRiderPosition(
    @ApiParam(value = "Current GPS latitude", required = true, example = "30.286804") @RequestParam Double locationLat,
    @ApiParam(value = "Current GPS longitude", required = true, example = "-97.707425") @RequestParam Double locationLng,
    @ApiParam(value = "Rider ID", required = true, example = "1") @RequestParam Long riderId
  ) throws ForbiddenException {
    return new GeolocationLogDto(
      geolocationLogService.addGeolocationLog(locationLat, locationLng, GeolocationLogEvent.RIDER_APP_OPEN, riderId)
    );
  }

}
