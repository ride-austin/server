package com.rideaustin.rest;

import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;
import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.maps.model.LatLng;
import com.rideaustin.DriverEndpoint;
import com.rideaustin.RiderEndpoint;
import com.rideaustin.assemblers.CurrentActiveDriverDtoEnricher;
import com.rideaustin.assemblers.MobileDriverRideDtoEnricher;
import com.rideaustin.model.enums.ActiveDriverStatus;
import com.rideaustin.model.enums.AvatarType;
import com.rideaustin.model.enums.GeolocationLogEvent;
import com.rideaustin.model.ride.ActiveDriver;
import com.rideaustin.model.ride.CarType;
import com.rideaustin.rest.exception.BadRequestException;
import com.rideaustin.rest.exception.RideAustinException;
import com.rideaustin.rest.exception.TermsNotAcceptedException;
import com.rideaustin.rest.model.CompactActiveDriverDto;
import com.rideaustin.rest.model.CurrentActiveDriverDto;
import com.rideaustin.service.ActiveDriverSearchService;
import com.rideaustin.service.ActiveDriverUpdateParams;
import com.rideaustin.service.ActiveDriversService;
import com.rideaustin.service.GeolocationLogService;
import com.rideaustin.service.RideService;
import com.rideaustin.service.config.GoOfflineConfig;
import com.rideaustin.service.user.CarTypesCache;
import com.rideaustin.service.user.RiderService;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/rest/acdr")
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class ActiveDrivers {

  @NonNull
  private final ActiveDriversService activeDriversService;
  @NonNull
  private final ActiveDriverSearchService activeDriversSearchService;
  @NonNull
  private final CarTypesCache carTypesCache;
  @NonNull
  private final CurrentActiveDriverDtoEnricher compactDtoEnricher;
  @NonNull
  private final MobileDriverRideDtoEnricher rideDtoEnricher;
  @NonNull
  private final GeolocationLogService geolocationLogService;
  @NonNull
  private final RiderService riderService;
  @NonNull
  private final GoOfflineConfig goOfflineConfig;
  private final RideService rideService;

  @DriverEndpoint
  @RolesAllowed(AvatarType.ROLE_DRIVER)
  @ApiOperation("Go online as a driver")
  @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponses({
    @ApiResponse(code = org.apache.http.HttpStatus.SC_OK, message = "Location of going online", response = LatLng.class),
    @ApiResponse(code = org.apache.http.HttpStatus.SC_BAD_REQUEST, message = "Driver is ineligible to go online using parameters passed"),
    @ApiResponse(code = org.apache.http.HttpStatus.SC_NOT_FOUND, message = "Unknown city ID is passed as a parameter"),
    @ApiResponse(code = org.apache.http.HttpStatus.SC_PRECONDITION_FAILED, message = "Driver has not accepted usage terms"),
    @ApiResponse(code = org.apache.http.HttpStatus.SC_INTERNAL_SERVER_ERROR, message = "Server is misconfigured, further processing is impossible")
  })
  public ResponseEntity<Object> activate(
    @ApiParam(value = "Current GPS latitude", required = true, example = "30.286804") @RequestParam Double latitude,
    @ApiParam(value = "Current GPS longitude", required = true, example = "-97.707425") @RequestParam Double longitude,
    @ApiParam(value = "Current GPS heading", example = "0.0") @RequestParam(required = false) Double heading,
    @ApiParam(value = "Current GPS speed", example = "0.0") @RequestParam(required = false) Double speed,
    @ApiParam(value = "Comma-separated list of enabled car categories", defaultValue = "REGULAR", required = true)
    @RequestParam(name = "carCategories", defaultValue = "REGULAR") Set<String> carCategories,
    @ApiParam(value = "Comma-separated list of enabled driver types") @RequestParam(required = false, name = "driverTypes") Set<String> driverTypes,
    @ApiParam(value = "Selected car ID", example = "1") @RequestParam(required = false) Long carId,
    @ApiParam(value = "Selected city ID", required = true, defaultValue = "1", example = "1") @RequestParam(defaultValue = "1") Long cityId) throws RideAustinException {

    try {
      return new ResponseEntity<>(
        activeDriversService.activate(new ActiveDriverUpdateParams(latitude, longitude, heading, null,
          speed, carCategories, driverTypes, carId, cityId)), HttpStatus.OK);
    } catch (TermsNotAcceptedException tna) {
      log.error("Terms were not accepted", tna);
      return new ResponseEntity<>(goOfflineConfig.getGoOfflineMessage(ActiveDriversService.GoOfflineEventSource.TERMS_NOT_ACCEPTED), HttpStatus.PRECONDITION_FAILED);
    }
  }

  @DriverEndpoint
  @RolesAllowed(AvatarType.ROLE_DRIVER)
  @ApiOperation("Update location as a driver")
  @PutMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponses({
    @ApiResponse(code = org.apache.http.HttpStatus.SC_OK, message = "Updated location", response = LatLng.class),
    @ApiResponse(code = org.apache.http.HttpStatus.SC_BAD_REQUEST, message = "Driver is ineligible to be online using parameters passed"),
    @ApiResponse(code = org.apache.http.HttpStatus.SC_CONFLICT, message = "Driver is already offline"),
    @ApiResponse(code = org.apache.http.HttpStatus.SC_INTERNAL_SERVER_ERROR, message = "Server is misconfigured, further processing is impossible")
  })
  public ResponseEntity update(
    @ApiParam(value = "Current GPS latitude", required = true, example = "30.286804") @RequestParam Double latitude,
    @ApiParam(value = "Current GPS longitude", required = true, example = "-97.707425") @RequestParam Double longitude,
    @ApiParam(value = "Current GPS heading", example = "0.0") @RequestParam(required = false) Double heading,
    @ApiParam(value = "Current GPS speed", example = "0.0") @RequestParam(required = false) Double speed,
    @ApiParam(value = "Current GPS course", example = "0.0") @RequestParam(required = false) Double course,
    @ApiParam(value = "Current device timestamp. Should be sent only when ride is started. Used for ride tracking", example = "1556791795")
    @RequestParam(required = false) Long sequence,
    @ApiParam(value = "Comma-separated list of enabled car categories", defaultValue = "REGULAR", required = true)
    @RequestParam(name = "carCategories", defaultValue = "REGULAR") Set<String> carCategories,
    @ApiParam(value = "Comma-separated list of enabled driver types") @RequestParam(name = "driverTypes", required = false) Set<String> driverTypes) throws RideAustinException {

    ActiveDriver driver = activeDriversService.getCurrentActiveDriver();
    if (driver == null) {
      return ResponseEntity.status(HttpStatus.CONFLICT).body("Driver is inactive");
    }

    ActiveDriverUpdateParams params = new ActiveDriverUpdateParams(latitude, longitude, heading, course,
      speed, carCategories, driverTypes, null, null);
    driver = activeDriversService.update(driver.getId(), params, sequence);
    return ResponseEntity.ok().body(activeDriversService.updateLocation(driver, params));
  }

  @DeleteMapping
  @DriverEndpoint
  @RolesAllowed(AvatarType.ROLE_DRIVER)
  @ApiOperation("Go offline as a driver")
  @ApiResponses({
    @ApiResponse(code = org.apache.http.HttpStatus.SC_OK, message = "Driver successfully went offline"),
    @ApiResponse(code = org.apache.http.HttpStatus.SC_BAD_REQUEST, message = "Driver tried to go offline while in a ride")
  })
  public void deactivate() throws BadRequestException {
    activeDriversService.deactivateAsDriver();
  }

  @RiderEndpoint
  @RolesAllowed(AvatarType.ROLE_RIDER)
  @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiOperation("Search for closest drivers eligible to driver given category and driver type as a rider")
  @ApiResponses({
    @ApiResponse(code = org.apache.http.HttpStatus.SC_OK, message = "List of driver objects", responseContainer = "List", response = CurrentActiveDriverDto.class),
    @ApiResponse(code = org.apache.http.HttpStatus.SC_UNAUTHORIZED, message = "User is not authenticated"),
    @ApiResponse(code = org.apache.http.HttpStatus.SC_FORBIDDEN, message = "User is not a rider"),
    @ApiResponse(code = org.apache.http.HttpStatus.SC_NOT_FOUND, message = "Current rider is not found")
  })
  public List<CompactActiveDriverDto> getClosestActiveDriversForRider(
    @ApiParam(value = "Current GPS latitude", required = true, example = "30.286804") @RequestParam Double latitude,
    @ApiParam(value = "Current GPS longitude", required = true, example = "-97.707425") @RequestParam Double longitude,
    @ApiParam(value = "Requested car category", required = true, defaultValue = "REGULAR") @RequestParam(defaultValue = "REGULAR") @Nullable String carCategory,
    @ApiParam(value = "Requested driver type") @RequestParam(required = false) @Nullable String driverType,
    @ApiParam(value = "City to look drivers in", required = true, defaultValue = "1", example = "1") @RequestParam(defaultValue = "1") Long cityId) throws RideAustinException {

    CarType carType = carTypesCache.getCarType(carCategory);

    List<CompactActiveDriverDto> result = activeDriversSearchService.findAvailableActiveDriversForRider(latitude, longitude, carType, driverType, cityId);

    geolocationLogService.addGeolocationLog(latitude, longitude, GeolocationLogEvent.GET_ACTIVE_DRIVERS_BY_RIDER, riderService.getCurrentRider().getId(), carType);

    return result;
  }

  @DriverEndpoint
  @RolesAllowed(AvatarType.ROLE_DRIVER)
  @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE, params = AvatarType.MAPPING_DRIVER)
  @ApiOperation("Search for closest drivers as a driver")
  @ApiResponses({
    @ApiResponse(code = org.apache.http.HttpStatus.SC_OK, message = "List of driver objects", responseContainer = "List", response = CurrentActiveDriverDto.class)
  })
  public List<CompactActiveDriverDto> getClosestActiveDriversForDriver(
    @ApiParam(value = "Current GPS latitude", required = true, example = "30.286804") @RequestParam Double latitude,
    @ApiParam(value = "Current GPS longitude", required = true, example = "-97.707425") @RequestParam Double longitude,
    @ApiParam(value = "City to look drivers in", required = true, defaultValue = "1", example = "1") @RequestParam(defaultValue = "1") Long cityId) {
    return activeDriversSearchService.findActiveDriversForDriver(cityId, latitude, longitude);
  }

  @DriverEndpoint
  @RolesAllowed(AvatarType.ROLE_DRIVER)
  @GetMapping(value = "current", produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiOperation("Get current online driver information as a driver")
  @ApiResponses({
    @ApiResponse(code = org.apache.http.HttpStatus.SC_OK, message = "Current driver object", response = CurrentActiveDriverDto.class),
    @ApiResponse(code = org.apache.http.HttpStatus.SC_NO_CONTENT, message = "Driver is offline"),
    @ApiResponse(code = org.apache.http.HttpStatus.SC_BAD_REQUEST, message = "Driver has more than 2 rides assigned simultaneously"),
    @ApiResponse(code = org.apache.http.HttpStatus.SC_FORBIDDEN, message = "User is not a driver"),
  })
  public ResponseEntity<CurrentActiveDriverDto> getCurrentActiveDriver() throws RideAustinException {
    CurrentActiveDriverDto activeDriver = activeDriversService.getCurrentActiveDriverForDriver();
    if (activeDriver == null) {
      return ResponseEntity.noContent().build();
    }
    CurrentActiveDriverDto enriched = compactDtoEnricher.enrich(activeDriver);
    if (ActiveDriverStatus.RIDING.equals(activeDriver.getStatus())) {
      enriched.setRide(rideDtoEnricher.enrich(rideService.getCurrentRideAsDriver()));
    }
    return ResponseEntity.ok(enriched);
  }

}