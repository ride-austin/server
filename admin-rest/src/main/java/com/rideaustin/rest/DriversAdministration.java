package com.rideaustin.rest;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;

import org.apache.commons.lang3.tuple.Pair;
import org.quartz.SchedulerException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.amazonaws.util.ComparableUtils;
import com.rideaustin.CheckedTransactional;
import com.rideaustin.WebClientEndpoint;
import com.rideaustin.assemblers.CarDtoAssembler;
import com.rideaustin.assemblers.SimpleDriverDtoAssembler;
import com.rideaustin.jobs.export.DriversExportJob;
import com.rideaustin.model.enums.AvatarType;
import com.rideaustin.model.ride.ActiveDriver;
import com.rideaustin.model.ride.Car;
import com.rideaustin.model.user.Driver;
import com.rideaustin.rest.exception.NotFoundException;
import com.rideaustin.rest.exception.RideAustinException;
import com.rideaustin.rest.model.ConsoleDriverDto;
import com.rideaustin.rest.model.DriverStatusDto;
import com.rideaustin.rest.model.DriverStatusPendingDto;
import com.rideaustin.rest.model.ListDriversParams;
import com.rideaustin.rest.model.OnboardingSortParams;
import com.rideaustin.rest.model.PagingParams;
import com.rideaustin.rest.model.SimpleDriverDto;
import com.rideaustin.service.ActiveDriversDisableService;
import com.rideaustin.service.ActiveDriversService;
import com.rideaustin.service.CurrentUserService;
import com.rideaustin.service.DriverAdministrationService;
import com.rideaustin.service.DriverService;
import com.rideaustin.service.SchedulerService;
import com.rideaustin.service.recovery.ActiveDriversRecoveryService;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@CheckedTransactional
@RequestMapping("/rest/drivers")
@RolesAllowed(AvatarType.ROLE_ADMIN)
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class DriversAdministration {

  private final SimpleDriverDtoAssembler driverDtoAssembler;
  private final CarDtoAssembler carDtoAssembler;
  private final ActiveDriversDisableService activeDriversDisableService;
  private final DriverAdministrationService driverAdministrationService;
  private final CurrentUserService currentUserService;
  private final SchedulerService schedulerService;
  private final ActiveDriversService activeDriversService;
  private final DriverService driverService;
  private final ActiveDriversRecoveryService recoveryService;

  @WebClientEndpoint
  @GetMapping("/payoneerStatus")
  @ApiOperation("Update Payoneer status for all driver who have pending Payoneer registration")
  @ApiResponses({
    @ApiResponse(code = org.apache.http.HttpStatus.SC_OK, message = "OK"),
    @ApiResponse(code = org.apache.http.HttpStatus.SC_INTERNAL_SERVER_ERROR, message = "Payoneer communication failure")
  })
  public ResponseEntity updatePayoneerStatus() {
    boolean success = driverAdministrationService.checkAndUpdatePayoneerStatusForPendingDrivers(true);
    return new ResponseEntity(success ? HttpStatus.OK : HttpStatus.INTERNAL_SERVER_ERROR);
  }

  @WebClientEndpoint
  @GetMapping("/list")
  @ApiOperation(value = "Get a list of drivers", responseContainer = "List", response = SimpleDriverDto.class)
  public Page<SimpleDriverDto> listDriversDto(@ApiParam @ModelAttribute ListDriversParams params, @ApiParam @ModelAttribute PagingParams paging) {
    OnboardingSortParams complexSort = OnboardingSortParams.from(paging.getSort());
    if (complexSort == null) {
      return driverDtoAssembler.toPageDto(driverAdministrationService.listDrivers(params, paging), paging.toPageRequest());
    } else {
      List<SimpleDriverDto> drivers = driverDtoAssembler.toDto(driverAdministrationService.listDrivers(params));
      int total = drivers.size();
      Comparator<SimpleDriverDto> comparator = (o1, o2) -> {
        Comparable<Object> val1 = complexSort.sortField().apply(o1);
        Comparable<Object> val2 = complexSort.sortField().apply(o2);
        return ComparableUtils.safeCompare(val1, val2);
      };
      if (paging.isDesc()) {
        comparator = comparator.reversed();
      }
      drivers = drivers
        .stream()
        .sorted(comparator)
        .skip((long) paging.getPage() * paging.getPageSize())
        .limit(paging.getPageSize())
        .collect(Collectors.toList());
      return new PageImpl<>(drivers, paging.toPageRequest(), total);
    }
  }

  @WebClientEndpoint
  @GetMapping("/statuses")
  @ApiOperation("Get count of drivers by their statuses")
  public DriverStatusDto listDriverStatuses(
    @ApiParam(value = "City ID", example = "1", required = true, defaultValue = "1") @RequestParam(defaultValue = "1") Long cityId
  ) {
    return new DriverStatusDto(driverAdministrationService.listDriverStatuses(cityId));
  }

  @WebClientEndpoint
  @GetMapping("/statuses/pending")
  @ApiOperation("Get counts of drivers in pending statuses by various criteria.")
  public DriverStatusPendingDto listDriverPendingStatuses(
    @ApiParam(value = "City ID", example = "1", required = true, defaultValue = "1") @RequestParam(defaultValue = "1") Long cityId
  ) {
    return driverAdministrationService.listDriverPendingStatuses(cityId);
  }

  @WebClientEndpoint
  @PostMapping("/export")
  @ApiOperation("Export filtered list of drivers as an email")
  @ApiResponses({
    @ApiResponse(code = org.apache.http.HttpStatus.SC_OK, message = "OK"),
    @ApiResponse(code = org.apache.http.HttpStatus.SC_INTERNAL_SERVER_ERROR, message = "Failed to schedule an export job")
  })
  public void exportDrivers(@ModelAttribute @ApiParam ListDriversParams params) throws SchedulerException {
    Map<String, Object> data = new HashMap<>();
    data.put("recipients", currentUserService.getUser().getEmail());
    data.put("params", params);
    schedulerService.triggerJob(DriversExportJob.class, data);
  }

  @WebClientEndpoint
  @PutMapping("/{id}/driverTypes")
  @ApiOperation("Assign driver types to a driver")
  @ApiResponses({
    @ApiResponse(code = org.apache.http.HttpStatus.SC_OK, message = "OK"),
    @ApiResponse(code = org.apache.http.HttpStatus.SC_NOT_FOUND, message = "Driver not found")
  })
  public void setDriverTypes(
    @ApiParam(value = "Driver ID", example = "1") @PathVariable long id,
    @ApiParam @RequestParam(required = false) List<String> driverTypes
  ) throws RideAustinException {
    driverAdministrationService.saveDriverTypes(id, driverTypes);
  }

  @WebClientEndpoint
  @PostMapping("/{id}/activationEmail")
  @ApiOperation("Send an activation email to a driver")
  @ApiResponses({
    @ApiResponse(code = org.apache.http.HttpStatus.SC_OK, message = "OK"),
    @ApiResponse(code = org.apache.http.HttpStatus.SC_NOT_FOUND, message = "Driver not found"),
    @ApiResponse(code = org.apache.http.HttpStatus.SC_BAD_REQUEST, message = "Driver is not activated"),
    @ApiResponse(code = org.apache.http.HttpStatus.SC_INTERNAL_SERVER_ERROR, message = "Failed to send an email")
  })
  public void sendActivationEmail(@ApiParam(value = "Driver ID", example = "1") @PathVariable long id) throws RideAustinException {
    driverAdministrationService.sendActivationEmail(id);
  }

  @WebClientEndpoint
  @DeleteMapping("/{id}/quickdisable")
  @ApiOperation("Disable driver immediately and set driver offline")
  @ApiResponses({
    @ApiResponse(code = org.apache.http.HttpStatus.SC_OK, message = "OK"),
    @ApiResponse(code = org.apache.http.HttpStatus.SC_INTERNAL_SERVER_ERROR, message = "Failed to send push notification")
  })
  public void disableDriverImmediately(@ApiParam(value = "Driver ID", example = "1") @PathVariable long id) throws RideAustinException {
    activeDriversDisableService.disableActiveDriverImmediately(id);
    driverAdministrationService.disableDriverImmediately(id);
  }

  @WebClientEndpoint
  @GetMapping("/{id}")
  @ApiOperation("Get information about a driver")
  @ApiResponses({
    @ApiResponse(code = org.apache.http.HttpStatus.SC_OK, message = "OK"),
    @ApiResponse(code = org.apache.http.HttpStatus.SC_NOT_FOUND, message = "Driver not found"),
    @ApiResponse(code = org.apache.http.HttpStatus.SC_INTERNAL_SERVER_ERROR, message = "Failed to update payoneer status")
  })
  public ConsoleDriverDto getDriver(@ApiParam(value = "Driver ID", example = "1") @PathVariable long id) throws RideAustinException {
    Pair<ConsoleDriverDto, Set<Car>> driverInfo = driverAdministrationService.getDriver(id);
    ConsoleDriverDto driver = driverInfo.getKey();
    driver.setCars(carDtoAssembler.toDto(driverInfo.getRight()));
    return driver;
  }

  @WebClientEndpoint
  @ApiOperation("Update driver information")
  @PutMapping(path = "/{id}", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
  @ApiResponses({
    @ApiResponse(code = org.apache.http.HttpStatus.SC_OK, message = "OK"),
    @ApiResponse(code = org.apache.http.HttpStatus.SC_BAD_REQUEST, message = "Provided data is invalid"),
    @ApiResponse(code = org.apache.http.HttpStatus.SC_NOT_FOUND, message = "Driver not found")
  })
  public ConsoleDriverDto updateDriverAsAdmin(
    @ApiParam(value = "Driver ID", example = "1") @PathVariable long id,
    @ApiParam @RequestBody ConsoleDriverDto driver
  ) throws RideAustinException {
    return driverAdministrationService.updateDriver(id, driver);
  }

  @WebClientEndpoint
  @PostMapping("/{id}/release")
  @ApiOperation("Make driver available if it's inconsistently set to RIDING state")
  @ApiResponses({
    @ApiResponse(code = org.apache.http.HttpStatus.SC_OK, message = "OK"),
    @ApiResponse(code = org.apache.http.HttpStatus.SC_NOT_FOUND, message = "Driver not found")
  })
  public void release(@ApiParam(value = "Driver ID", example = "1") @PathVariable long id) throws NotFoundException {
    final Driver driver = driverService.findDriver(id);
    final ActiveDriver activeDriver = activeDriversService.getActiveDriverByDriver(driver.getUser());
    recoveryService.setAsAvailable(activeDriver.getId());
  }
}
