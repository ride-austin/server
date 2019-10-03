package com.rideaustin.rest;

import java.util.Collection;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;

import org.apache.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.rideaustin.WebClientEndpoint;
import com.rideaustin.model.enums.AvatarType;
import com.rideaustin.model.user.Avatar;
import com.rideaustin.rest.exception.NotFoundException;
import com.rideaustin.rest.exception.RideAustinException;
import com.rideaustin.service.DriverService;
import com.rideaustin.service.notifications.PushNotificationsFacade;
import com.rideaustin.service.notifications.impl.AmazonPushNotificationsService;
import com.rideaustin.service.notifications.model.Topic;
import com.rideaustin.service.user.RiderService;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;

@RestController
@RolesAllowed(AvatarType.ROLE_ADMIN)
@RequestMapping("/rest/notifications")
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class PushNotifications {

  private final AmazonPushNotificationsService pushNotificationsService;
  private final PushNotificationsFacade pushNotificationsFacade;
  private final RiderService riderService;
  private final DriverService driverService;

  @WebClientEndpoint
  @ApiOperation("List of avaialable notification topics")
  @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponses({
    @ApiResponse(code = HttpStatus.SC_OK, message = "OK")
  })
  public Collection<Topic> listTopics() {
    return pushNotificationsService.listTopics();
  }

  @WebClientEndpoint
  @ApiOperation("Push a notification to a topic")
  @PostMapping(value = "/{topicId}", produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponses({
    @ApiResponse(code = HttpStatus.SC_OK, message = "OK")
  })
  public void pushNotification(
    @ApiParam(value = "Topic ID", example = "1") @PathVariable Long topicId,
    @ApiParam("Notification content") @RequestParam String message
  ) {
    pushNotificationsService.pushTextNotification(topicId, message);
  }

  @PostMapping("/{avatarType}/{avatarId}")
  @ApiOperation("Push a notification to a specific user")
  @ApiResponses({
    @ApiResponse(code = HttpStatus.SC_OK, message = "OK"),
    @ApiResponse(code = HttpStatus.SC_NOT_FOUND, message = "User not found")
  })
  public void pushNotification(
    @ApiParam(value = "Avatar type", allowableValues = "RIDER,DRIVER") @PathVariable String avatarType,
    @ApiParam(value = "Avatar ID", example = "1") @PathVariable Long avatarId,
    @ApiParam("Notification content") @RequestParam String message
  ) throws RideAustinException {
    Avatar avatar = null;
    AvatarType type = AvatarType.valueOf(avatarType.toUpperCase());
    if (type == AvatarType.RIDER) {
      avatar = riderService.findRider(avatarId);
    } else if (type == AvatarType.DRIVER) {
      avatar = driverService.findDriver(avatarId);
    }
    if (avatar == null) {
      throw new NotFoundException("Avatar not found");
    }
    pushNotificationsFacade.pushTextNotification(avatar.getUser(), type, message);
  }
}
