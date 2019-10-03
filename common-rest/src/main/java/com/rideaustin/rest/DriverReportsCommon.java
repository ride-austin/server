package com.rideaustin.rest;

import java.time.Instant;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;

import org.apache.http.HttpStatus;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.rideaustin.DriverEndpoint;
import com.rideaustin.model.enums.AvatarType;
import com.rideaustin.model.user.Driver;
import com.rideaustin.repo.dsl.RideDslRepository;
import com.rideaustin.rest.exception.RideAustinException;
import com.rideaustin.rest.model.DriverRide;
import com.rideaustin.rest.model.PagingParams;
import com.rideaustin.service.CurrentUserService;
import com.rideaustin.service.DriverService;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class DriverReportsCommon {

  private final CurrentUserService cuSvc;
  private final DriverService driverService;
  private final RideDslRepository rideDslRepository;

  @DriverEndpoint
  @RolesAllowed({AvatarType.ROLE_DRIVER, AvatarType.ROLE_ADMIN})
  @GetMapping(value = "/rest/drivers/{id}/rides", produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiOperation(value = "Get a report on rides performed by a driver", responseContainer = "List", response = DriverRide.class)
  @ApiResponses({
    @ApiResponse(code = HttpStatus.SC_OK, message = "OK"),
    @ApiResponse(code = HttpStatus.SC_FORBIDDEN, message = "Requesting user is not a driver or admin"),
    @ApiResponse(code = HttpStatus.SC_NOT_FOUND, message = "Driver not found"),
  })
  public Page<DriverRide> getDriverRides(@ApiParam(value = "Driver ID", example = "1") @PathVariable long id,
    @ApiParam(value = "Report start datetime", example = "2000-10-31T01:30:00.000-05:00") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant completedOnAfter,
    @ApiParam(value = "Report end datetime", example = "2000-10-31T01:30:00.000-05:00") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant completedOnBefore,
    @ApiParam @ModelAttribute PagingParams paging) throws RideAustinException {
    Driver driver = driverService.findDriver(id, cuSvc.getUser());
    //@Hotfix for issue RA-1792
    paging.setPageSize(500);

    return rideDslRepository.getPageableDriverEarnings(driver, completedOnAfter, completedOnBefore, paging);
  }
}
