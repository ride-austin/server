package com.rideaustin.rest;

import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;

import org.apache.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.rideaustin.RiderEndpoint;
import com.rideaustin.assemblers.SplitFareDtoAssembler;
import com.rideaustin.model.enums.AvatarType;
import com.rideaustin.rest.exception.RideAustinException;
import com.rideaustin.rest.model.SplitFareDto;
import com.rideaustin.service.farepayment.SplitFareService;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;

@RiderEndpoint
@RestController
@RequestMapping("/rest/splitfares")
@RolesAllowed(AvatarType.ROLE_RIDER)
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class SplitFares {

  private final SplitFareService farePaymentService;
  private final SplitFareDtoAssembler splitFareDtoAssembler;

  @PostMapping("/{rideId}")
  @ApiOperation("Send split fare request for a ride")
  @ApiResponses({
    @ApiResponse(code = HttpStatus.SC_OK, message = "OK"),
    @ApiResponse(code = HttpStatus.SC_FORBIDDEN, message = "Fare can't be split"),
    @ApiResponse(code = HttpStatus.SC_NOT_FOUND, message = "Rider not found")
  })
  public SplitFareDto sendSplitFareRequest(
    @ApiParam(value = "Ride ID", example = "1") @PathVariable Long rideId,
    @ApiParam(value = "Comma-separated list of recipient phone numbers", required = true) @RequestParam List<String> phoneNumbers
  ) throws RideAustinException {
    return splitFareDtoAssembler.toDto(farePaymentService.sendSplitFareRequest(rideId, phoneNumbers));
  }

  @ApiOperation("Accept or decline split fare request")
  @PostMapping(value = "/{splitFareId}/accept", produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponses({
    @ApiResponse(code = HttpStatus.SC_OK, message = "OK"),
    @ApiResponse(code = HttpStatus.SC_FORBIDDEN, message = "Request can't be accepted or declined"),
    @ApiResponse(code = HttpStatus.SC_NOT_FOUND, message = "Request not found")
  })
  public SplitFareDto changeAcceptanceStatus(
    @ApiParam(value = "Split fare request ID", example = "1") @PathVariable Long splitFareId,
    @ApiParam("Accept or decline request") @RequestParam Boolean acceptance
  ) throws RideAustinException {
    return splitFareDtoAssembler.toDto(farePaymentService.changeAcceptanceStatus(splitFareId, acceptance));
  }

  @ApiOperation("Get a list of split fare requests per ride")
  @GetMapping(value = "/{rideId}/list", produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponses({
    @ApiResponse(code = HttpStatus.SC_OK, message = "OK"),
    @ApiResponse(code = HttpStatus.SC_FORBIDDEN, message = "Requesting rider is not a participant of the ride"),
    @ApiResponse(code = HttpStatus.SC_NOT_FOUND, message = "Ride not found")
  })
  public List<SplitFareDto> listForRide(
    @ApiParam(value = "Ride ID", example = "1") @PathVariable Long rideId
  ) throws RideAustinException {
    return farePaymentService.getListForRide(rideId);
  }

  @ApiOperation("Remove split fare request and associated rider from the ride")
  @DeleteMapping(value = "/{splitFareId}", produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponses({
    @ApiResponse(code = HttpStatus.SC_OK, message = "OK"),
    @ApiResponse(code = HttpStatus.SC_FORBIDDEN, message = "Requesting rider is not a participant of the ride"),
    @ApiResponse(code = HttpStatus.SC_NOT_FOUND, message = "Ride or split fare request not found")
  })
  public void removeRiderFromSplitting(
    @ApiParam(value = "Split fare request ID", example = "1") @PathVariable Long splitFareId
  ) throws RideAustinException {
    farePaymentService.removeRiderFromSplitting(splitFareId);
  }

  @ApiOperation("Get a list of pending split fare requests per rider")
  @GetMapping(value = "/{riderId}/requested", produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponses({
    @ApiResponse(code = HttpStatus.SC_OK, message = "OK"),
    @ApiResponse(code = HttpStatus.SC_FORBIDDEN, message = "Requesting rider is not a participant of the ride"),
    @ApiResponse(code = HttpStatus.SC_NOT_FOUND, message = "Rider not found")
  })
  public List<SplitFareDto> getPendingSplitFareRequest(
    @ApiParam(value = "Rider ID", example = "1") @PathVariable Long riderId
  ) throws RideAustinException {
    return farePaymentService.findPendingSplitFareRequestForRider(riderId);
  }

}
