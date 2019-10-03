package com.rideaustin.rest;

import java.time.Instant;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;

import org.apache.http.HttpStatus;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.rideaustin.DriverEndpoint;
import com.rideaustin.model.enums.AvatarType;
import com.rideaustin.model.user.Driver;
import com.rideaustin.rest.exception.RideAustinException;
import com.rideaustin.service.ActiveDriverReportService;
import com.rideaustin.service.CurrentUserService;
import com.rideaustin.service.DriverService;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/rest/drivers")
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class DriverReports {

  private final CurrentUserService cuSvc;
  private final DriverService driverService;
  private final ActiveDriverReportService activeDriverReportService;

  @DriverEndpoint
  @RolesAllowed(AvatarType.ROLE_DRIVER)
  @ApiOperation("Get statistics on online time for a driver")
  @GetMapping(value = "{id}/online", produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponses({
    @ApiResponse(code = HttpStatus.SC_OK, message = "OK"),
    @ApiResponse(code = HttpStatus.SC_FORBIDDEN, message = "Requesting user is not a driver"),
    @ApiResponse(code = HttpStatus.SC_NOT_FOUND, message = "Driver not found")
  })
  public DriverOnline getSecondsOnline(@ApiParam(value = "Driver ID", example = "1") @PathVariable long id,
    @ApiParam(value = "Report start time", required = true, example = "2000-10-31T01:30:00.000-05:00") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
    @ApiParam(value = "Report end time", required = true, example = "2000-10-31T01:30:00.000-05:00") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to)
    throws RideAustinException {
    Driver driver = driverService.findDriver(id, cuSvc.getUser());
    return new DriverOnline(activeDriverReportService.getDriverOnlineSeconds(driver, from, to));
  }

  @Getter
  @ApiModel
  @RequiredArgsConstructor
  private static class DriverOnline {
    @ApiModelProperty(required = true)
    private final long seconds;
  }

}
