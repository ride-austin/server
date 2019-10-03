package com.rideaustin.rest;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;

import org.apache.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.rideaustin.DriverEndpoint;
import com.rideaustin.MobileClientEndpoint;
import com.rideaustin.RiderEndpoint;
import com.rideaustin.model.enums.AvatarType;
import com.rideaustin.rest.exception.NotFoundException;
import com.rideaustin.rest.exception.RideAustinException;
import com.rideaustin.service.ride.RideUpgradeService;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/rest/rides/upgrade")
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class RideUpgrades {

  private static final String UPGRADE_REQUEST_NOT_FOUND = "Upgrade request is not found or already expired";

  private final RideUpgradeService upgradeService;

  @DriverEndpoint
  @PostMapping("request")
  @RolesAllowed(AvatarType.ROLE_DRIVER)
  @ApiOperation("Request ride upgrade as a driver")
  @ApiResponses({
    @ApiResponse(code = HttpStatus.SC_OK, message = "OK"),
    @ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = "Upgrade can not be requested")
  })
  public RideUpgradeResponse request(
    @ApiParam(value = "Target car type", required = true, allowableValues = "SUV") @RequestParam String target
  ) throws RideAustinException {
    upgradeService.requestUpgrade(target);
    return new RideUpgradeResponse("Your request has been submitted to rider");
  }

  @RiderEndpoint
  @PostMapping("accept")
  @RolesAllowed(AvatarType.ROLE_RIDER)
  @ApiOperation("Accept ride upgrade as a rider")
  @ApiResponses({
    @ApiResponse(code = HttpStatus.SC_OK, message = "OK"),
    @ApiResponse(code = HttpStatus.SC_FORBIDDEN, message = "Requesting user is not a rider"),
    @ApiResponse(code = HttpStatus.SC_NOT_FOUND, message = "Upgrade request not found")
  })
  public RideUpgradeResponse accept() throws RideAustinException {
    boolean success = upgradeService.acceptRequest();
    if (success) {
      return new RideUpgradeResponse("You have accepted ride upgrade request");
    } else {
      throw new NotFoundException(UPGRADE_REQUEST_NOT_FOUND);
    }
  }

  @MobileClientEndpoint
  @RolesAllowed(AvatarType.ROLE_DRIVER)
  @ApiOperation("Cancel ride upgrade as a driver")
  @PostMapping(value = "decline", params = AvatarType.MAPPING_DRIVER)
  @ApiResponses({
    @ApiResponse(code = HttpStatus.SC_OK, message = "OK"),
    @ApiResponse(code = HttpStatus.SC_FORBIDDEN, message = "Requesting user is not a driver"),
    @ApiResponse(code = HttpStatus.SC_NOT_FOUND, message = "Upgrade request not found")
  })
  public RideUpgradeResponse cancel() throws RideAustinException {
    boolean success = upgradeService.cancelRequest();
    if (success) {
      return new RideUpgradeResponse("Your request has been cancelled");
    } else {
      throw new NotFoundException(UPGRADE_REQUEST_NOT_FOUND);
    }
  }

  @RolesAllowed(AvatarType.ROLE_RIDER)
  @ApiOperation("Decline ride upgrade as a rider")
  @PostMapping(value = "decline", params = AvatarType.MAPPING_RIDER)
  @ApiResponses({
    @ApiResponse(code = HttpStatus.SC_OK, message = "OK"),
    @ApiResponse(code = HttpStatus.SC_FORBIDDEN, message = "Requesting user is not a rider"),
    @ApiResponse(code = HttpStatus.SC_NOT_FOUND, message = "Upgrade request not found")
  })
  public RideUpgradeResponse decline() throws RideAustinException {
    boolean success = upgradeService.declineRequest();
    if (success) {
      return new RideUpgradeResponse("You have declined ride upgrade request");
    } else {
      throw new NotFoundException(UPGRADE_REQUEST_NOT_FOUND);
    }
  }

  @RolesAllowed(AvatarType.ROLE_RIDER)
  @ApiOperation("Decline ride upgrade as a dispatcher")
  @PostMapping(value = "decline", params = AvatarType.MAPPING_DISPATCHER)
  @ApiResponses({
    @ApiResponse(code = HttpStatus.SC_OK, message = "OK"),
    @ApiResponse(code = HttpStatus.SC_FORBIDDEN, message = "Requesting user is not a rider"),
    @ApiResponse(code = HttpStatus.SC_NOT_FOUND, message = "Upgrade request not found")
  })
  public RideUpgradeResponse declineAsDispatcher() throws RideAustinException {
    return decline();
  }

  @Getter
  @ApiModel
  @RequiredArgsConstructor
  static class RideUpgradeResponse {
    @ApiModelProperty(required = true)
    final String message;
  }
}
