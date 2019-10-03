package com.rideaustin.rest;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;

import com.google.maps.model.LatLng;
import com.rideaustin.DriverEndpoint;
import com.rideaustin.MobileClientEndpoint;
import com.rideaustin.RiderEndpoint;
import com.rideaustin.WebClientEndpoint;
import com.rideaustin.assemblers.DirectConnectHistoryDtoEnricher;
import com.rideaustin.assemblers.MobileDispatcherRideDtoEnricher;
import com.rideaustin.assemblers.MobileDriverRideDtoEnricher;
import com.rideaustin.assemblers.MobileRiderRideDtoEnricher;
import com.rideaustin.model.enums.AvatarType;
import com.rideaustin.model.enums.RideStatus;
import com.rideaustin.model.fee.SpecialFee;
import com.rideaustin.model.ride.CarType;
import com.rideaustin.model.ride.Ride;
import com.rideaustin.model.user.Rider;
import com.rideaustin.model.user.User;
import com.rideaustin.repo.dsl.RideDslRepository;
import com.rideaustin.rest.exception.BadRequestException;
import com.rideaustin.rest.exception.ForbiddenException;
import com.rideaustin.rest.exception.RideAustinException;
import com.rideaustin.rest.model.DirectConnectHistoryDto;
import com.rideaustin.rest.model.DispatcherAccountRideDto;
import com.rideaustin.rest.model.EstimatedFareDTO;
import com.rideaustin.rest.model.Location;
import com.rideaustin.rest.model.MobileDriverRideDto;
import com.rideaustin.rest.model.MobileRiderRideDto;
import com.rideaustin.rest.model.RideEvents;
import com.rideaustin.rest.model.UrlDto;
import com.rideaustin.service.CityService;
import com.rideaustin.service.CurrentUserService;
import com.rideaustin.service.FareEstimateService;
import com.rideaustin.service.FareService;
import com.rideaustin.service.RideService;
import com.rideaustin.service.ride.RideEvent;
import com.rideaustin.service.ride.RideEventsBuilder;
import com.rideaustin.service.ride.RideOwnerService;
import com.rideaustin.service.ride.SequentialRideEventsDispatcher;
import com.rideaustin.service.thirdparty.S3StorageService;
import com.rideaustin.service.user.CarTypesCache;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/rest/rides")
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class Rides {

  private final CurrentUserService cuSvc;
  private final RideDslRepository rideDslRepo;
  private final FareService fareService;
  private final FareEstimateService fareEstimateService;
  private final S3StorageService s3StorageService;
  private final RideService rideService;
  private final RideOwnerService rideOwnerService;
  private final SequentialRideEventsDispatcher sequentialRideEventsDispatcher;
  private final CarTypesCache carTypesCache;
  private final CityService cityService;
  private final RideEventsBuilder rideEventsBuilder;
  private final MobileDriverRideDtoEnricher mobileDriverRideDtoEnricher;
  private final MobileRiderRideDtoEnricher<MobileRiderRideDto> rideDtoEnricher;
  private final MobileDispatcherRideDtoEnricher dispatcherRideDtoEnricher;
  private final DirectConnectHistoryDtoEnricher directConnectHistoryDtoEnricher;

  @DriverEndpoint
  @RolesAllowed(AvatarType.ROLE_DRIVER)
  @ApiOperation("Get ride object as a driver")
  @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE, params = AvatarType.MAPPING_DRIVER)
  @ApiResponses({
    @ApiResponse(code = HttpStatus.SC_OK, message = "OK")
  })
  public MobileDriverRideDto getRideAsDriver(
    @ApiParam(value = "Ride ID", example = "1") @PathVariable long id
  ) {
    MobileDriverRideDto rideDto = rideDslRepo.findOneForDriver(id);
    if (rideDto != null && RideStatus.REQUESTED.equals(rideDto.getStatus()) || rideOwnerService.isDriversRide(id)) {
      return mobileDriverRideDtoEnricher.enrich(rideDto);
    }
    return null;
  }

  @RiderEndpoint
  @RolesAllowed(AvatarType.ROLE_RIDER)
  @ApiOperation("Get ride object as a rider")
  @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE, params = AvatarType.MAPPING_RIDER)
  @ApiResponses({
    @ApiResponse(code = HttpStatus.SC_OK, message = "OK"),
    @ApiResponse(code = HttpStatus.SC_FORBIDDEN, message = "Requesting rider is not an owner of the ride")
  })
  public MobileRiderRideDto getRideAsRider(
    @ApiParam(value = "Ride ID", example = "1") @PathVariable long id,
    @ApiParam @ModelAttribute Location location
  ) throws ForbiddenException {
    return rideDtoEnricher.enrich(rideService.getRideAsRider(id, location.getLat(), location.getLng()));
  }

  @RiderEndpoint
  @RolesAllowed(AvatarType.ROLE_RIDER)
  @ApiOperation("Get ride object as a dispatcher")
  @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE, params = AvatarType.MAPPING_DISPATCHER)
  @ApiResponses({
    @ApiResponse(code = HttpStatus.SC_OK, message = "OK"),
    @ApiResponse(code = HttpStatus.SC_FORBIDDEN, message = "Requesting rider is not an owner of the ride")
  })
  public DispatcherAccountRideDto getRideAsDispatcher(
    @ApiParam(value = "Ride ID", example = "1") @PathVariable long id
  ) throws ForbiddenException {
    if (!cuSvc.getUser().getAvatar(Rider.class).isDispatcherAccount()) {
      throw new ForbiddenException();
    }
    return dispatcherRideDtoEnricher.toDto(rideService.getRideAsDispatcher(id));
  }

  @DriverEndpoint
  @RolesAllowed(AvatarType.ROLE_DRIVER)
  @ApiOperation(value = "Get current ride as a driver")
  @GetMapping(value = "/current", produces = MediaType.APPLICATION_JSON_VALUE, params = AvatarType.MAPPING_DRIVER)
  @ApiResponses({
    @ApiResponse(code = HttpStatus.SC_OK, message = "OK"),
    @ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = "Current driver is offline")
  })
  public MobileDriverRideDto getCurrentRideAsDriver() throws BadRequestException {
    return mobileDriverRideDtoEnricher.enrich(rideService.getCurrentRideAsDriver());
  }

  @RiderEndpoint
  @RolesAllowed(AvatarType.ROLE_RIDER)
  @ApiOperation(value = "Get current ride as a driver")
  @GetMapping(value = "/current", produces = MediaType.APPLICATION_JSON_VALUE, params = AvatarType.MAPPING_RIDER)
  @ApiResponses({
    @ApiResponse(code = HttpStatus.SC_OK, message = "OK")
  })
  public MobileRiderRideDto getCurrentRideAsRider() {
    return rideDtoEnricher.enrich(rideService.getCurrentRideAsRider());
  }

  @RiderEndpoint
  @RolesAllowed(AvatarType.ROLE_RIDER)
  @ApiOperation(value = "Get current ride as a dispatcher")
  @GetMapping(value = "/current", produces = MediaType.APPLICATION_JSON_VALUE, params = AvatarType.MAPPING_DISPATCHER)
  @ApiResponses({
    @ApiResponse(code = HttpStatus.SC_OK, message = "OK"),
    @ApiResponse(code = HttpStatus.SC_FORBIDDEN, message = "Requesting rider is not a dispatcher")
  })
  public List<DispatcherAccountRideDto> getCurrentRideAsDispatcher() throws ForbiddenException {
    if (!cuSvc.getUser().getAvatar(Rider.class).isDispatcherAccount()) {
      throw new ForbiddenException();
    }
    return dispatcherRideDtoEnricher.toDto(rideService.getCurrentRidesAsDispatcher());
  }

  @RiderEndpoint
  @GetMapping("/last")
  @RolesAllowed(AvatarType.ROLE_RIDER)
  @ApiOperation("Get last unrated ride as a rider")
  @ApiResponses({
    @ApiResponse(code = HttpStatus.SC_OK, message = "OK")
  })
  public MobileRiderRideDto getLastRide() {
    return rideDtoEnricher.enrich(rideService.getLastUnratedRide());
  }

  @RiderEndpoint
  @RolesAllowed(AvatarType.ROLE_RIDER)
  @GetMapping(value = "/last", params = AvatarType.MAPPING_DISPATCHER)
  public List<DispatcherAccountRideDto> getLastRides() throws ForbiddenException {
    if (!cuSvc.getUser().getAvatar(Rider.class).isDispatcherAccount()) {
      throw new ForbiddenException();
    }
    return dispatcherRideDtoEnricher.toDto(rideService.getLastUnratedRides());
  }

  @RiderEndpoint
  @GetMapping(value = "/estimate", produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiOperation(value = "Fare estimate",
    notes =
      "Estimates fare between start and end location. " +
        "No value should be provided for cityId to estimate in Austin. " +
        "No value should be provided for carCategory if we estimate for STANDARD car category")
  @RolesAllowed({AvatarType.ROLE_API_CLIENT, AvatarType.ROLE_RIDER})
  @ApiResponses({
    @ApiResponse(code = HttpStatus.SC_OK, message = "OK"),
    @ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = "Car category or city is invalid; estimation failed")
  })
  public EstimatedFareDTO estimateFare(
    @ApiParam(value = "Start location latitude", example = "30.286804", required = true) @RequestParam double startLat,
    @ApiParam(value = "Start location longitude", example = "-97.707425", required = true) @RequestParam double startLong,
    @ApiParam(value = "End location latitude", example = "30.286804", required = true) @RequestParam double endLat,
    @ApiParam(value = "End location longitude", example = "-97.707425", required = true) @RequestParam double endLong,
    @ApiParam("Car category") @RequestParam(defaultValue = "REGULAR", required = false) String carCategory,
    @ApiParam(value = "City ID", example = "1") @RequestParam(defaultValue = "1") Long cityId
  ) throws RideAustinException {
    CarType cartype = carTypesCache.getCarType(carCategory);
    cityService.getCityOrThrow(cityId);
    if (cartype == null) {
      throw new BadRequestException("Please select a car category");
    }
    Optional<EstimatedFareDTO> estimatedFare = fareEstimateService.estimateFare(new LatLng(startLat, startLong), new LatLng(endLat, endLong),
      cartype, cityId);

    return estimatedFare.orElseThrow(() -> new BadRequestException("Unable to estimate ride fare. Check start and end locations"));
  }

  @RiderEndpoint
  @RolesAllowed(AvatarType.ROLE_RIDER)
  @GetMapping(value = "/specialFees", produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiOperation(value = "specialFees", notes = "Set of special fees - applicable for specified pickup location.")
  @ApiResponses({
    @ApiResponse(code = HttpStatus.SC_OK, message = "OK")
  })
  public List<SpecialFee> specialFees(
    @ApiParam(value = "Latitude", example = "30.286804", required = true) @RequestParam double startLat,
    @ApiParam(value = "Longitude", example = "-97.707425", required = true) @RequestParam double startLong
  ) {
    return fareService.getSpecialFees(new LatLng(startLat, startLong));
  }

  @WebClientEndpoint
  @MobileClientEndpoint
  @ApiOperation("Get ride map URL")
  @GetMapping(path = "/{id}/map", produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponses({
    @ApiResponse(code = HttpStatus.SC_OK, message = "OK"),
    @ApiResponse(code = HttpStatus.SC_FORBIDDEN, message = "Requesting rider is not an owner of the ride"),
    @ApiResponse(code = HttpStatus.SC_NOT_FOUND, message = "Ride not found")
  })
  public UrlDto getRideMap(
    @ApiParam(value = "Ride ID", example = "1") @PathVariable long id
  ) throws RideAustinException {
    Ride ride = rideService.getRide(id);
    User user = cuSvc.getUser();
    if (!user.isAdmin() && !rideOwnerService.isRideRider(id) && !rideOwnerService.isDriversRide(id)) {
      throw new ForbiddenException();
    }

    if (StringUtils.isEmpty(ride.getRideMap())) {
      log.info("Ride map is empty!!");
      return null;
    }
    return new UrlDto(s3StorageService.getSignedURL(ride.getRideMap()));
  }

  @DriverEndpoint
  @RolesAllowed(AvatarType.ROLE_DRIVER)
  @ApiOperation(value = "Process events cached on app side", response = Object.class)
  @PostMapping(value = "/events", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponses({
    @ApiResponse(code = HttpStatus.SC_OK, message = "OK")
  })
  public DeferredResult<ResponseEntity> processEvents(@RequestBody RideEvents rideEvents) {
    DeferredResult<ResponseEntity> result = new DeferredResult<>(50L, ResponseEntity.ok().build());
    List<RideEvent> events = rideEventsBuilder.buildEvents(rideEvents)
      .stream()
      .filter(e -> rideOwnerService.isDriversRide(e.getRideId()))
      .sorted(Comparator.comparing(RideEvent::getRideId).thenComparing(RideEvent::getTimestamp))
      .collect(Collectors.toList());
    sequentialRideEventsDispatcher.dispatchEvents(events, result);
    return result;
  }

  @GetMapping(value = "history/direct")
  @ApiOperation("Get history of Direct Connect rides")
  @RolesAllowed({AvatarType.ROLE_RIDER, AvatarType.ROLE_ADMIN})
  @ApiResponses({
    @ApiResponse(code = HttpStatus.SC_OK, message = "OK")
  })
  public List<DirectConnectHistoryDto> getDirectConnectHistory(
    @ApiParam(value = "Rider ID", example = "1") @RequestParam(required = false) Long riderId
  ) {
    Long rider;
    if (riderId != null && cuSvc.getUser().isAdmin()) {
      rider = riderId;
    } else {
      rider = cuSvc.getUser().getAvatar(Rider.class).getId();
    }
    return directConnectHistoryDtoEnricher.toDto(rideDslRepo.getDirectConnectHistoryForRider(rider));
  }

}
