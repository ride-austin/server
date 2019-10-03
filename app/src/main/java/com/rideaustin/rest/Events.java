package com.rideaustin.rest;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.http.HttpStatus;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;

import com.rideaustin.MobileClientEndpoint;
import com.rideaustin.NewRelicIgnoreTransaction;
import com.rideaustin.model.enums.AvatarType;
import com.rideaustin.model.user.Avatar.Info;
import com.rideaustin.model.user.User;
import com.rideaustin.rest.exception.ForbiddenException;
import com.rideaustin.rest.model.EventDto;
import com.rideaustin.service.CurrentUserService;
import com.rideaustin.service.event.EventManager;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@RestController
@RequestMapping("/rest/events")
public class Events {

  private final boolean devMode;
  private final Long pollTimeOut;

  private final CurrentUserService cuSvc;
  private final EventManager eventMgr;

  @Inject
  public Events(CurrentUserService cuSvc, Environment env, EventManager eventMgr) {
    this.cuSvc = cuSvc;
    this.eventMgr = eventMgr;
    this.pollTimeOut = env.getProperty("poll.timeout.milliseconds", Long.class, 55000L);
    this.devMode = ArrayUtils.contains(env.getActiveProfiles(), "dev");
  }

  @GetMapping
  @MobileClientEndpoint
  @NewRelicIgnoreTransaction
  @RolesAllowed({AvatarType.ROLE_RIDER, AvatarType.ROLE_DRIVER})
  @ApiOperation(value = "Long-polling list of events", response = EventDto.class, responseContainer = "List")
  @ApiResponses({
    @ApiResponse(code = HttpStatus.SC_OK, message = "OK"),
    @ApiResponse(code = HttpStatus.SC_FORBIDDEN, message = "Requesting user doesn't have avatar set")
  })
  public DeferredResult<List<EventDto>> getEvents(
    @ApiParam(value = "Avatar type", allowableValues = "RIDER,DRIVER") @RequestParam AvatarType avatarType,
    @ApiParam("ID of last received event") @RequestParam(required = false) String lastReceivedEvent) throws ForbiddenException {

    User user = cuSvc.getUser();
    Info info = user.avatarInfo(avatarType);

    // Check if the user has the specified avatar
    if (info == null) {
      throw new ForbiddenException();
    }
    DeferredResult<List<EventDto>> deferredResult = new DeferredResult<>(pollTimeOut, Collections.emptyList());

    Long eventId = devMode ?
      null :
      Optional.ofNullable(lastReceivedEvent)
        .map(s -> s.replace(' ', '+'))
        .map(Double::valueOf)
        .map(Double::longValue)
        .orElse(null);
    eventMgr.register(info, user, eventId, deferredResult);
    return deferredResult;
  }
}
