package com.rideaustin.rest;

import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;

import org.apache.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rideaustin.WebClientEndpoint;
import com.rideaustin.model.enums.AvatarType;
import com.rideaustin.rest.exception.RideAustinException;
import com.rideaustin.rest.model.RatingUpdateDto;
import com.rideaustin.service.DriverService;
import com.rideaustin.service.rating.RatingUpdateService;
import com.rideaustin.service.user.RiderService;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/rest/ratingupdates")
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class RatingUpdates {

  private final RatingUpdateService ratingUpdateService;
  private final DriverService driverService;
  private final RiderService riderService;

  @WebClientEndpoint
  @RolesAllowed({AvatarType.ROLE_ADMIN, AvatarType.ROLE_DRIVER})
  @ApiOperation("Get a list of rating updates as a driver / for a driver")
  @GetMapping(value = "/forDriver/{driverId}", produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponses({
    @ApiResponse(code = HttpStatus.SC_OK, message = "OK"),
    @ApiResponse(code = HttpStatus.SC_NOT_FOUND, message = "Driver not found")
  })
  public List<RatingUpdateDto> listRatingUpdatesForDriver(
    @ApiParam(value = "Driver ID", example = "1") @PathVariable Long driverId
  ) throws RideAustinException {
    return ratingUpdateService.getRatingUpdateFor(driverService.findDriver(driverId));
  }

  @WebClientEndpoint
  @RolesAllowed({AvatarType.ROLE_ADMIN, AvatarType.ROLE_RIDER})
  @ApiOperation("Get a list of rating updates as a rider / for a rider")
  @GetMapping(value = "/forRider/{riderId}", produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponses({
    @ApiResponse(code = HttpStatus.SC_OK, message = "OK"),
    @ApiResponse(code = HttpStatus.SC_NOT_FOUND, message = "Rider not found")
  })
  public List<RatingUpdateDto> listRatingUpdatesForRider(
    @ApiParam(value = "Rider ID", example = "1") @PathVariable Long riderId
  ) throws RideAustinException {
    return ratingUpdateService.getRatingUpdateFor(riderService.findRider(riderId));
  }

  @WebClientEndpoint
  @RolesAllowed({AvatarType.ROLE_ADMIN, AvatarType.ROLE_DRIVER})
  @ApiOperation("Get a list of rating updates submitted as a driver / by a driver")
  @GetMapping(value = "/byDriver/{driverId}", produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponses({
    @ApiResponse(code = HttpStatus.SC_OK, message = "OK"),
    @ApiResponse(code = HttpStatus.SC_NOT_FOUND, message = "Driver not found")
  })
  public List<RatingUpdateDto> listRatingUpdatesByDriver(
    @ApiParam(value = "Driver ID", example = "1") @PathVariable Long driverId
  ) throws RideAustinException {
    return ratingUpdateService.getRatingUpdateBy(driverService.findDriver(driverId));
  }

  @WebClientEndpoint
  @RolesAllowed({AvatarType.ROLE_ADMIN, AvatarType.ROLE_RIDER})
  @ApiOperation("Get a list of rating updates submitted as a rider / by a rider")
  @GetMapping(value = "/byRider/{riderId}", produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponses({
    @ApiResponse(code = HttpStatus.SC_OK, message = "OK"),
    @ApiResponse(code = HttpStatus.SC_NOT_FOUND, message = "Rider not found")
  })
  public List<RatingUpdateDto> listRatingUpdatesByRider(
    @ApiParam(value = "Rider ID", example = "1") @PathVariable Long riderId
  ) throws RideAustinException {
    return ratingUpdateService.getRatingUpdateBy(riderService.findRider(riderId));
  }

}
