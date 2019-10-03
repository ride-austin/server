package com.rideaustin.rest;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;

import org.apache.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.rideaustin.CheckedTransactional;
import com.rideaustin.DriverEndpoint;
import com.rideaustin.model.enums.AvatarType;
import com.rideaustin.model.user.Driver;
import com.rideaustin.rest.exception.RideAustinException;
import com.rideaustin.rest.model.AreaQueuePositions;
import com.rideaustin.service.CurrentUserService;
import com.rideaustin.service.DriverService;
import com.rideaustin.service.areaqueue.AreaQueueService;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;

@RestController
@CheckedTransactional
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class DriverAreaQueueCommon {

  private final CurrentUserService cuSvc;
  private final DriverService driverService;
  private final AreaQueueService areaQueueService;

  @DriverEndpoint
  @RolesAllowed({AvatarType.ROLE_DRIVER, AvatarType.ROLE_ADMIN})
  @ApiOperation("Get current driver's position in all present queues")
  @GetMapping(path = "/rest/drivers/{driverId}/queue", produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponses({
    @ApiResponse(code = HttpStatus.SC_OK, message = "OK"),
    @ApiResponse(code = HttpStatus.SC_FORBIDDEN, message = "Requesting user is not a driver"),
    @ApiResponse(code = HttpStatus.SC_NOT_FOUND, message = "Driver not found")
  })
  public AreaQueuePositions getCurrentDriverPositionInQueue(@ApiParam(value = "Driver ID", example = "1") @PathVariable long driverId) throws RideAustinException {
    Driver driver = driverService.findDriver(driverId, cuSvc.getUser());
    return areaQueueService.calculateDriverCurrentPositionInQueue(driver);
  }
}
