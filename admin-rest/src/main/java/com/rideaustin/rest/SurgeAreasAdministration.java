package com.rideaustin.rest;

import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.rideaustin.WebClientEndpoint;
import com.rideaustin.assemblers.SurgeAreaDtoAssembler;
import com.rideaustin.model.enums.AvatarType;
import com.rideaustin.model.surgepricing.SurgeArea;
import com.rideaustin.rest.exception.BadRequestException;
import com.rideaustin.rest.exception.NotFoundException;
import com.rideaustin.rest.exception.RideAustinException;
import com.rideaustin.rest.model.GlobalPriorityFareDto;
import com.rideaustin.rest.model.SurgeAreaDto;
import com.rideaustin.service.config.SurgeMode;
import com.rideaustin.service.surgepricing.SurgePricingService;
import com.rideaustin.service.surgepricing.SurgeRecalculationConfig;
import com.rideaustin.service.surgepricing.SurgeRecalculationConfigProvider;
import com.rideaustin.service.surgepricing.SurgeRecalculationServiceFactory;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/rest/surgeareas")
@RolesAllowed(AvatarType.ROLE_ADMIN)
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class SurgeAreasAdministration {

  private final SurgePricingService surgePricingService;
  private final SurgeRecalculationServiceFactory recalculationServiceFactory;
  private final SurgeRecalculationConfigProvider configProvider;
  private final SurgeAreaDtoAssembler assembler;

  @WebClientEndpoint
  @ApiOperation("Update surge area properties")
  @PutMapping(value = "/{surgeAreaId}",
    consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponses({
    @ApiResponse(code = org.apache.http.HttpStatus.SC_OK, message = "OK"),
    @ApiResponse(code = org.apache.http.HttpStatus.SC_BAD_REQUEST, message = "Provided data is invalid"),
    @ApiResponse(code = org.apache.http.HttpStatus.SC_NOT_FOUND, message = "Surge area not found")
  })
  public SurgeAreaDto updateSurgeArea(
    @ApiParam(value = "Surge area ID", example = "1") @PathVariable Long surgeAreaId,
    @ApiParam("Surge area object") @Valid @RequestBody SurgeAreaDto surgeAreaDto
  ) throws RideAustinException {
    surgePricingService.updateSurgeArea(surgeAreaId, assembler.toDs(surgeAreaDto), true);
    return surgeAreaDto;
  }

  @WebClientEndpoint
  @ApiOperation("Create a new surge area")
  @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponses({
    @ApiResponse(code = org.apache.http.HttpStatus.SC_OK, message = "OK"),
    @ApiResponse(code = org.apache.http.HttpStatus.SC_BAD_REQUEST, message = "Provided data is invalid")
  })
  public SurgeAreaDto createSurgeArea(
    @ApiParam("Surge area object") @Valid @RequestBody SurgeAreaDto surgeArea
  ) throws RideAustinException {
    SurgeArea createdSurgeArea = surgePricingService.createSurgeArea(assembler.toDs(surgeArea));
    surgeArea.setId(createdSurgeArea.getId());
    return surgeArea;
  }

  @WebClientEndpoint
  @ApiOperation(value = "Bulk update surge areas")
  @PutMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponses({
    @ApiResponse(code = org.apache.http.HttpStatus.SC_OK, message = "OK. List of surge area names that failed to update")
  })
  public List<String> updateSurgeAreas(
    @ApiParam("List of surge area objects") @Valid @RequestBody List<SurgeAreaDto> surgeAreas
  ) {
    return surgePricingService.updateSurgeAreas(assembler.toDs(surgeAreas));
  }

  @WebClientEndpoint
  @DeleteMapping("/{surgeAreaId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @ApiOperation("Remove existing surge area")
  @ApiResponses({
    @ApiResponse(code = org.apache.http.HttpStatus.SC_NO_CONTENT, message = "OK"),
    @ApiResponse(code = org.apache.http.HttpStatus.SC_NOT_FOUND, message = "Surge area not found")
  })
  public void removeSurgeArea(
    @ApiParam(value = "Surge area ID", example = "1") @PathVariable Long surgeAreaId
  ) throws NotFoundException {
    surgePricingService.removeSurgeArea(surgeAreaId);
  }

  @WebClientEndpoint
  @GetMapping(value = "/config")
  @ApiOperation("Get current surge recalculation configuration")
  @ApiResponses({
    @ApiResponse(code = org.apache.http.HttpStatus.SC_OK, message = "OK")
  })
  public SurgeRecalculationConfig config(
    @ApiParam(value = "City ID", example = "1", required = true, defaultValue = "1") @RequestParam(defaultValue = "1") Long cityId
  ) {
    return configProvider.getConfig(cityId);
  }

  @WebClientEndpoint
  @PostMapping("/config")
  @ApiOperation("Update surge recalculation configuration")
  @ApiResponses({
    @ApiResponse(code = org.apache.http.HttpStatus.SC_OK, message = "OK"),
    @ApiResponse(code = org.apache.http.HttpStatus.SC_BAD_REQUEST, message = "Provided config is invalid")
  })
  public SurgeRecalculationConfig updateConfig(
    @ApiParam(value = "City ID", example = "1", required = true, defaultValue = "1") @RequestParam(defaultValue = "1") Long cityId,
    @ApiParam("Config object") @RequestBody SurgeRecalculationConfig config
  ) throws BadRequestException {
    boolean result = recalculationServiceFactory.createRecalculationService(cityId).updateConfig(cityId, config);
    if (!result) {
      throw new BadRequestException(config.toString());
    }
    return config;
  }

  @WebClientEndpoint
  @PatchMapping("/config")
  @ApiOperation("Update surge recalculation mode")
  @ApiResponses({
    @ApiResponse(code = org.apache.http.HttpStatus.SC_OK, message = "OK"),
    @ApiResponse(code = org.apache.http.HttpStatus.SC_BAD_REQUEST, message = "Provided config is invalid")
  })
  public GlobalPriorityFareDto updatePriorityFareMode(
    @ApiParam("Global priority config object") @Valid @RequestBody GlobalPriorityFareDto globalPriorityFareDto
  ) throws BadRequestException {
    SurgeMode surgeMode = SurgeMode.fromKey(globalPriorityFareDto.getSurgeMode());
    if (surgeMode != null) {
      boolean result = recalculationServiceFactory
        .createRecalculationService(globalPriorityFareDto.getCityId())
        .updatePriorityFareMode(globalPriorityFareDto.getCityId(), surgeMode);
      if (!result) {
        throw new BadRequestException(globalPriorityFareDto.toString());
      }
      return globalPriorityFareDto;
    } else {
      throw new BadRequestException(globalPriorityFareDto.toString());
    }
  }

}
