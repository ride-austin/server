package com.rideaustin.rest;

import java.math.RoundingMode;
import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.rideaustin.CheckedTransactional;
import com.rideaustin.WebClientEndpoint;
import com.rideaustin.assemblers.AdminRideDtoEnricher;
import com.rideaustin.assemblers.ExtendedRideDtoEnricher;
import com.rideaustin.assemblers.MapInfoDtoEnricher;
import com.rideaustin.assemblers.RideHistoryDtoEnricher;
import com.rideaustin.model.enums.AvatarType;
import com.rideaustin.model.enums.RideStatus;
import com.rideaustin.model.ride.Ride;
import com.rideaustin.repo.dsl.RideAdministrationDslRepository;
import com.rideaustin.rest.exception.BadRequestException;
import com.rideaustin.rest.exception.RideAustinException;
import com.rideaustin.rest.model.CompactRideDto;
import com.rideaustin.rest.model.ConsoleRideDto;
import com.rideaustin.rest.model.ExtendedRideDto;
import com.rideaustin.rest.model.ListRidesParams;
import com.rideaustin.rest.model.MapInfoDto;
import com.rideaustin.rest.model.PagingParams;
import com.rideaustin.rest.model.RideHistoryDto;
import com.rideaustin.service.ActiveDriversService;
import com.rideaustin.service.RideService;
import com.rideaustin.service.RideTrackerService;
import com.rideaustin.service.model.OnlineDriverDto;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/rest/rides")
@RolesAllowed(AvatarType.ROLE_ADMIN)
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class RidesAdministration {

  private final RideAdministrationDslRepository rideDslRepo;

  private final ActiveDriversService activeDriversService;
  private final RideService rideService;
  private final RideTrackerService rideTrackerService;

  private final ExtendedRideDtoEnricher extendedRideDtoEnricher;
  private final MapInfoDtoEnricher mapInfoDtoEnricher;
  private final AdminRideDtoEnricher adminRideDtoEnricher;
  private final RideHistoryDtoEnricher rideHistoryDtoEnricher;

  @WebClientEndpoint
  @ApiOperation(value = "Get a list of rides (history format) as an administrator", response = RideHistoryDto.class, responseContainer = "List")
  @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE, params = AvatarType.MAPPING_ADMIN)
  @ApiResponses({
    @ApiResponse(code = org.apache.http.HttpStatus.SC_OK, message = "OK")
  })
  public Page<RideHistoryDto> listRidesAsAdminFull(
    @ApiParam @ModelAttribute ListRidesParams params,
    @ApiParam @ModelAttribute PagingParams paging
  ) {
    return rideDslRepo.findAdminRideInfoPage(params, paging).map(rideHistoryDtoEnricher);
  }

  @WebClientEndpoint
  @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE, params = {AvatarType.MAPPING_ADMIN, "format=compact"})
  @ApiOperation(value = "Get a list of rides (compact format) as an administrator", response = CompactRideDto.class, responseContainer = "List")
  @ApiResponses({
    @ApiResponse(code = org.apache.http.HttpStatus.SC_OK, message = "OK")
  })
  public Page<CompactRideDto> listRidesAsAdminCompact(
    @ApiParam @ModelAttribute ListRidesParams params,
    @ApiParam @ModelAttribute PagingParams paging
  ) {
    Page<CompactRideDto> rides = rideDslRepo.findRidesCompact(params, paging);
    return rides.map(source -> {
      if (source.getDistanceTravelled() != null) {
        source.setDistanceTravelled(source.getDistanceTravelled().setScale(2, RoundingMode.HALF_UP));
      }
      return source;
    });
  }

  @WebClientEndpoint
  @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE, path = "/list")
  @ApiOperation(value = "Get a list of rides (extended format) as an administrator", response = ExtendedRideDto.class, responseContainer = "List")
  @ApiResponses({
    @ApiResponse(code = org.apache.http.HttpStatus.SC_OK, message = "OK")
  })
  public Iterable<ExtendedRideDto> listRidesExtendedDto(
    @ApiParam @ModelAttribute ListRidesParams params,
    @ApiParam @ModelAttribute PagingParams paging
  ) {
    Page<ExtendedRideDto> ridesResult = rideDslRepo.findRidesExtended(params, paging);
    return ridesResult.map(extendedRideDtoEnricher);
  }

  @WebClientEndpoint
  @ApiOperation("Get a ride object as an administrator")
  @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE, params = AvatarType.MAPPING_ADMIN)
  public ConsoleRideDto getRideAsAdmin(
    @ApiParam(value = "Ride ID", example = "1") @PathVariable long id
  ) {
    ConsoleRideDto adminRideInfo = rideDslRepo.findAdminRideInfo(id);
    return adminRideDtoEnricher.enrich(adminRideInfo);
  }

  @WebClientEndpoint
  @GetMapping("map")
  @ApiOperation("Get map information")
  public List<MapInfoDto> map(
    @ApiParam(value = "City ID", example = "1", defaultValue = "1", required = true) @RequestParam(defaultValue = "1") Long cityId
  ) {
    List<OnlineDriverDto> activeDrivers = activeDriversService.getActiveDrivers();
    List<MapInfoDto> mapInfo = rideDslRepo.findMapInfo(cityId);
    return mapInfoDtoEnricher.enrich(mapInfo, activeDrivers);
  }

  @CheckedTransactional
  @ApiOperation("Recreate missing ride map")
  @PutMapping(path = "/{id}/map/recreate", produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponses({
    @ApiResponse(code = org.apache.http.HttpStatus.SC_OK, message = "OK"),
    @ApiResponse(code = org.apache.http.HttpStatus.SC_BAD_REQUEST, message = "Ride is not completed")
  })
  public void recreateMap(
    @ApiParam(value = "Ride ID", example = "1") @PathVariable long id
  ) throws RideAustinException {
    Ride ride = rideService.getRide(id);

    if (!RideStatus.COMPLETED.equals(ride.getStatus())) {
      throw new BadRequestException("Ride is not completed");
    }
    rideTrackerService.saveStaticImage(ride);
  }

  @WebClientEndpoint
  @PostMapping("/{id}/receipt")
  @ApiOperation("Resend a receipt")
  @ResponseStatus(HttpStatus.ACCEPTED)
  @ApiResponses({
    @ApiResponse(code = org.apache.http.HttpStatus.SC_OK, message = "OK"),
    @ApiResponse(code = org.apache.http.HttpStatus.SC_INTERNAL_SERVER_ERROR, message = "Failed to send email")
  })
  public void resendReceipt(@PathVariable long id) throws RideAustinException {
    rideService.resendReceipt(id);
  }

}
