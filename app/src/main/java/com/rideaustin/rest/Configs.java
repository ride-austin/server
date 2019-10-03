package com.rideaustin.rest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import com.rideaustin.DriverEndpoint;
import com.rideaustin.MobileClientEndpoint;
import com.rideaustin.RiderEndpoint;
import com.rideaustin.assemblers.CityCarTypeDtoAssembler;
import com.rideaustin.assemblers.CityDriverTypeDtoAssembler;
import com.rideaustin.clients.configuration.ClientConfigurationService;
import com.rideaustin.filter.ClientType;
import com.rideaustin.model.AppInfo;
import com.rideaustin.model.enums.AvatarType;
import com.rideaustin.model.enums.PlatformType;
import com.rideaustin.repo.dsl.CampaignProviderDslRepository;
import com.rideaustin.rest.exception.BadRequestException;
import com.rideaustin.rest.exception.RideAustinException;
import com.rideaustin.rest.model.CarModelInfo;
import com.rideaustin.rest.model.Location;
import com.rideaustin.service.AppInfoService;
import com.rideaustin.service.CarTypeService;
import com.rideaustin.service.CityService;
import com.rideaustin.service.ride.DriverTypeService;
import com.rideaustin.service.thirdparty.S3StorageService;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/rest/configs")
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class Configs {

  private static final String CAR_TYPES = "carTypes";

  private final DriverTypeService driverTypeService;
  private final CityDriverTypeDtoAssembler driverTypeDtoAssembler;
  private final CarTypeService carTypeService;
  private final CityCarTypeDtoAssembler carTypeDtoAssembler;
  private final CityService cityService;
  private final AppInfoService appInfoService;
  private final ClientConfigurationService clientConfigurationService;
  private final S3StorageService s3StorageService;

  private final CampaignProviderDslRepository campaignProviderDslRepository;

  private final ObjectMapper objectMapper;

  @DriverEndpoint
  @GetMapping(path = "/driver/global", produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiOperation(value = "Dynamically generated configuration JSON object for driver application", responseContainer = "Map")
  @ApiResponses({
    @ApiResponse(code = org.apache.http.HttpStatus.SC_OK, message = "OK"),
    @ApiResponse(code = org.apache.http.HttpStatus.SC_BAD_REQUEST, message = "Latitude or longitude are invalid"),
    @ApiResponse(code = org.apache.http.HttpStatus.SC_INTERNAL_SERVER_ERROR, message = "Server is misconfigured, configuration JSON can not be parsed")
  })
  public Map<String, Object> globalDriverConfiguration(
    @ApiParam("Current driver's GPS location") @ModelAttribute Location location,
    @ApiParam(value = "City ID to get configuration for", example = "1") @RequestParam(required = false) Long cityId,
    @ApiParam("Set of requested configuration parameters. Endpoint will return entire configuration object if this parameter is missing")
    @RequestParam(required = false) Set<String> configAttributes) throws RideAustinException {
    validateLocation(location);
    Long city = Optional.ofNullable(cityId).orElseGet(() -> getCityForLocation(location));
    return ImmutableMap.<String, Object>builder()
      .putAll(clientConfigurationService.getConfiguration(ClientType.DRIVER, location, city, configAttributes))
      .put(CAR_TYPES, carTypeDtoAssembler.toDto(carTypeService.getCityCarTypes(city)))
      .build();
  }

  @RiderEndpoint
  @GetMapping(path = "/rider/global", produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiOperation(value = "Dynamically generated configuration JSON object for rider application", responseContainer = "Map")
  @ApiResponses({
    @ApiResponse(code = org.apache.http.HttpStatus.SC_OK, message = "OK"),
    @ApiResponse(code = org.apache.http.HttpStatus.SC_BAD_REQUEST, message = "Latitude or longitude are invalid"),
    @ApiResponse(code = org.apache.http.HttpStatus.SC_INTERNAL_SERVER_ERROR, message = "Server is misconfigured, configuration JSON can not be parsed")
  })
  public Map<String, Object> globalRiderConfiguration(
    @ApiParam("Current rider's GPS location") @ModelAttribute Location location,
    @ApiParam(value = "City ID to get configuration for", example = "1") @RequestParam(required = false) Long cityId,
    @ApiParam("Set of requested configuration parameters. Endpoint will return entire configuration object if this parameter is missing")
    @RequestParam(required = false) Set<String> configAttributes) throws RideAustinException {
    validateLocation(location);
    Long city = Optional.ofNullable(cityId).orElseGet(() -> getCityForLocation(location));
    Map<String, Object> defaultConfiguration = clientConfigurationService.getConfiguration(ClientType.RIDER, location, city, configAttributes);
    ImmutableMap.Builder<String, Object> result = ImmutableMap.<String, Object>builder()
      .putAll(defaultConfiguration)
      .put("driverTypes", driverTypeDtoAssembler.toDto(
        driverTypeService.getCityDriverTypes(city)).stream().filter(cdt -> cdt.getConfiguration().isVisibleToRider()).collect(Collectors.toList())
      )
      .put(CAR_TYPES, carTypeDtoAssembler.toDto(carTypeService.getCityCarTypes(city)))
      .put("campaignProviders", campaignProviderDslRepository.getAll(city));
    return result.build();
  }

  @MobileClientEndpoint
  @GetMapping(path = "/app/info/current", produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiOperation("Get latest released application information to enable force/mandatory upgrade on app side")
  public AppInfo getAppInfo(
    @ApiParam(value = "Application type", allowableValues = "RIDER,DRIVER", required = true) @RequestParam AvatarType avatarType,
    @ApiParam(value = "Platform type", required = true) @RequestParam PlatformType platformType
  ) {
    return appInfoService.getAppInfo(avatarType, platformType);
  }

  @MobileClientEndpoint
  @GetMapping(path = "/cars", produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiOperation(value = "Get all supported cars makes and models to show in driver app", response = CarModelInfo.class,
  responseContainer = "List")
  @ApiResponses({
    @ApiResponse(code = org.apache.http.HttpStatus.SC_OK, message = "OK"),
    @ApiResponse(code = org.apache.http.HttpStatus.SC_NOT_MODIFIED, message = "Cars list was not modified since date provided in If-Modified-Since header")
  })
  public ResponseEntity<List<CarModelInfo>> supportedCarsList(
    @RequestHeader(value = HttpHeaders.IF_MODIFIED_SINCE, required = false) Date ifModifiedSince) {
    try {
      byte[] content = s3StorageService.loadCachedFile("cars.json", ifModifiedSince);
      if (content.length == 0) {
        return ResponseEntity.status(HttpStatus.NOT_MODIFIED).build();
      }
      return ResponseEntity.ok(objectMapper.readValue(content, objectMapper.getTypeFactory().constructCollectionType(ArrayList.class, CarModelInfo.class)));
    } catch (IOException e) {
      log.error("Failed to read config", e);
      return ResponseEntity.notFound().build();
    }
  }

  private void validateLocation(Location location) throws BadRequestException {
    if ((location.getLat() == null && location.getLng() != null) ||
      (location.getLng() == null && location.getLat() != null)) {
      throw new BadRequestException("Invalid coordinates");
    }
  }

  private Long getCityForLocation(Location location) {
    return cityService.findClosestByCoordinates(location).getId();
  }

}
