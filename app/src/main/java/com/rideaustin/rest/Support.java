package com.rideaustin.rest;

import java.util.Optional;

import javax.inject.Inject;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.rideaustin.MobileClientEndpoint;
import com.rideaustin.RiderEndpoint;
import com.rideaustin.filter.ClientAppVersion;
import com.rideaustin.filter.ClientAppVersionContext;
import com.rideaustin.filter.ClientType;
import com.rideaustin.model.enums.AvatarType;
import com.rideaustin.model.user.User;
import com.rideaustin.rest.exception.RideAustinException;
import com.rideaustin.rest.model.SupportRequestDto;
import com.rideaustin.service.CityService;
import com.rideaustin.service.CurrentUserService;
import com.rideaustin.service.support.SupportService;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/rest/support")
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class Support {

  private final SupportService supportService;
  private final CurrentUserService cuSvc;
  private final CityService cityService;

  @RiderEndpoint
  @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiOperation("Send a message to customer support regarding a ride")
  @ApiResponses({
    @ApiResponse(code = HttpStatus.SC_OK, message = "OK"),
    @ApiResponse(code = HttpStatus.SC_NOT_FOUND, message = "Ride not found"),
    @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = "Failed to send support message")
  })
  public void sendSupportMessage(
    @ApiParam(value = "Support message content", required = true) @RequestParam String message,
    @ApiParam(value = "Ride ID", example = "1") @RequestParam(required = false) Long rideId,
    @ApiParam(value = "City ID", example = "1") @RequestParam(required = false) Long cityId
  ) throws RideAustinException {
    final String escapedMessage = StringEscapeUtils.escapeHtml4(message);
    User user = cuSvc.getUser();
    ClientType clientType = ClientAppVersionContext.getAppVersion().getClientType();

    long resolvedCity = Optional.ofNullable(cityId).orElse(cityService.getCityForCurrentClientAppVersionContext().getId());
    if (rideId != null) {
      supportService.sendRideSupportEmail(user, escapedMessage, rideId, resolvedCity, clientType);
    } else {
      supportService.sendGenericSupportEmail(user, escapedMessage, resolvedCity, clientType);
    }
  }

  @MobileClientEndpoint
  @ApiOperation("Send a generic customer support message")
  @PostMapping(value = "/default", produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponses({
    @ApiResponse(code = HttpStatus.SC_OK, message = "OK"),
    @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = "Failed to send email message")
  })
  public void sendSupportMessage(
    @ApiParam("Support request object") @RequestBody SupportRequestDto supportRequestDto
  ) throws RideAustinException {
    User user = cuSvc.getUser();
    ClientAppVersion appVersion = ClientAppVersionContext.getAppVersion();
    AvatarType avatarType = AvatarType.ADMIN;
    if (appVersion.getClientType() == ClientType.DRIVER) {
      avatarType = AvatarType.DRIVER;
    } else if (appVersion.getClientType() == ClientType.RIDER) {
      avatarType = AvatarType.RIDER;
    }
    supportService.sendDefaultSupportEmail(user, avatarType, supportRequestDto.getRideId(),
      supportRequestDto.getTopicId(), supportRequestDto.getComments());
  }
}

