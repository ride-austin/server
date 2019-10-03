package com.rideaustin.rest;

import java.util.List;

import javax.inject.Inject;

import org.apache.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.rideaustin.CheckedTransactional;
import com.rideaustin.ExternalEndpoint;
import com.rideaustin.MobileClientEndpoint;
import com.rideaustin.assemblers.RideTrackingShareDtoAssembler;
import com.rideaustin.model.ride.Ride;
import com.rideaustin.rest.exception.NotFoundException;
import com.rideaustin.rest.exception.RideAustinException;
import com.rideaustin.rest.model.Location;
import com.rideaustin.rest.model.RideTrackingShareDto;
import com.rideaustin.rest.model.TrackingShareToken;
import com.rideaustin.service.RideRealTimeTrackingService;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/rest/rides")
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class RideShareTracking {

  private final RideRealTimeTrackingService rideRealTimeTrackingService;
  private final RideTrackingShareDtoAssembler rideTrackingShareDtoAssembler;

  @CheckedTransactional
  @ApiOperation("Send an email with a link to share the ride route")
  @PostMapping(value = "/{id}/shareToEmail", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponses({
    @ApiResponse(code = HttpStatus.SC_OK, message = "OK"),
    @ApiResponse(code = HttpStatus.SC_FORBIDDEN, message = "Ride doesn't belong requesting rider"),
    @ApiResponse(code = HttpStatus.SC_NOT_FOUND, message = "Ride not found"),
    @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = "Failed to send email")
  })
  public void shareRideToFollow(
    @ApiParam(value = "Ride ID", example = "1") @PathVariable("id") Long rideId,
    @ApiParam(value = "Recipient email", required = true) @RequestParam String emailAddress
  ) throws RideAustinException {
    rideRealTimeTrackingService.shareRideToFollow(rideId, emailAddress);
  }

  @MobileClientEndpoint
  @CheckedTransactional
  @ApiOperation("Get sharing token for a ride")
  @PostMapping(value = "/{id}/getShareToken", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponses({
    @ApiResponse(code = HttpStatus.SC_OK, message = "OK"),
    @ApiResponse(code = HttpStatus.SC_FORBIDDEN, message = "Ride doesn't belong requesting rider"),
    @ApiResponse(code = HttpStatus.SC_NOT_FOUND, message = "Ride not found")
  })
  public TrackingShareToken getTrackingShareToken(
    @ApiParam(value = "Ride ID", example = "1") @PathVariable("id") Long rideId
  ) throws RideAustinException {
    return rideRealTimeTrackingService.getShareToken(rideId);
  }

  @ExternalEndpoint
  @CheckedTransactional
  @GetMapping("/{trackingKey}/allTrackers")
  @ApiOperation("Get current ride track for web sharing console page")
  @ApiResponses({
    @ApiResponse(code = HttpStatus.SC_OK, message = "OK"),
    @ApiResponse(code = HttpStatus.SC_NOT_FOUND, message = "Ride not found")
  })
  public RideTrackingShareDto getCurrentRideTracking(
    @ApiParam("Sharing tracking key") @PathVariable String trackingKey
  ) throws NotFoundException {
    Ride ride = rideRealTimeTrackingService.getRideForTrackingShareToken(trackingKey);
    List<Location> locations = rideRealTimeTrackingService.getCurrentRideTrackingLocations(ride);
    RideTrackingShareDto dto = rideTrackingShareDtoAssembler.toDto(ride);
    dto.setLocations(locations);
    return dto;
  }
}
