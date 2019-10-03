package com.rideaustin.rest;

import java.util.Date;
import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;

import org.apache.http.HttpStatus;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.rideaustin.WebClientEndpoint;
import com.rideaustin.model.ChangeDto;
import com.rideaustin.model.enums.AvatarType;
import com.rideaustin.model.user.Driver;
import com.rideaustin.repo.dsl.DriverDslRepository;
import com.rideaustin.rest.exception.NotFoundException;
import com.rideaustin.service.DriverAuditedService;
import com.rideaustin.service.generic.TimeService;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;

@RestController
@RolesAllowed(AvatarType.ROLE_ADMIN)
@RequestMapping("/rest/driver-changes")
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class DriverChanges {

  private final DriverAuditedService driverAuditedService;
  private final DriverDslRepository driverDslRepository;
  private final TimeService timeService;

  @WebClientEndpoint
  @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiOperation("Get a list of changes submitted to a driver profile by date")
  @ApiResponses({
    @ApiResponse(code = HttpStatus.SC_OK, message = "OK"),
    @ApiResponse(code = HttpStatus.SC_NOT_FOUND, message = "Driver not found")
  })
  public List<ChangeDto> getDriverChanges(@ApiParam(value = "Driver ID", required = true, example = "1") @RequestParam Long driverId,
    @ApiParam(value = "Day to get changes for", example = "2019-12-31") @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date auditDay) throws NotFoundException {
    Driver driver = driverDslRepository.findById(driverId);
    if (driver == null) {
      throw new NotFoundException("Driver not found!");
    }
    Date resolvedAuditDay = auditDay;
    if (resolvedAuditDay == null) {
      resolvedAuditDay = timeService.getCurrentDate();
    }
    return driverAuditedService.getDriverChanges(driver, resolvedAuditDay);
  }
}