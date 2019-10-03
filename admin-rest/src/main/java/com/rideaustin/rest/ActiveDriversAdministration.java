package com.rideaustin.rest;

import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;

import org.apache.http.HttpStatus;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.rideaustin.CheckedTransactional;
import com.rideaustin.WebClientEndpoint;
import com.rideaustin.assemblers.ActiveDriverDtoEnricher;
import com.rideaustin.assemblers.ActiveDriverLocationDtoAssembler;
import com.rideaustin.model.enums.AvatarType;
import com.rideaustin.rest.exception.BadRequestException;
import com.rideaustin.rest.model.ActiveDriverDto;
import com.rideaustin.rest.model.ActiveDriverLocationDto;
import com.rideaustin.rest.model.PagingParams;
import com.rideaustin.service.ActiveDriversService;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/rest/acdr")
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class ActiveDriversAdministration {

  private final ActiveDriversService activeDriversService;
  private final ActiveDriverDtoEnricher dtoEnricher;
  private final ActiveDriverLocationDtoAssembler locationDtoAssembler;

  @WebClientEndpoint
  @CheckedTransactional
  @DeleteMapping("{id}")
  @RolesAllowed(AvatarType.ROLE_ADMIN)
  @ApiOperation("Force end driver offline")
  @ApiResponses({
    @ApiResponse(code = HttpStatus.SC_OK, message = "Driver is forced to go offline"),
    @ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = "Driver is in a ride, so it's not possible to send offline")
  })
  public void deactivate(@ApiParam(value = "Active Driver ID", example = "1") @PathVariable long id) throws BadRequestException {
    activeDriversService.deactivateAsAdmin(id);
  }

  @WebClientEndpoint
  @ApiOperation("List online drivers for web admin console")
  @RolesAllowed(AvatarType.ROLE_ADMIN)
  @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE, params = AvatarType.MAPPING_ADMIN)
  public Page<ActiveDriverDto> getClosestActiveDriversForAdminPageable(
    @ApiParam(value = "City ID to search online drivers in", example = "1") @RequestParam(required = false) Long cityId,
    @ApiParam("Paging params") @ModelAttribute PagingParams paging) {
    return activeDriversService.getActiveDriversPage(cityId, paging).map(dtoEnricher);
  }

  @WebClientEndpoint
  @RolesAllowed(AvatarType.ROLE_API_CLIENT)
  @ApiOperation("List online drivers for 3rd party API clients")
  @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE, params = AvatarType.MAPPING_API_CLIENT)
  public List<ActiveDriverLocationDto> getClosestActiveDriversForApiClient(
    @ApiParam("Paging params") @ModelAttribute PagingParams paging) {
    return locationDtoAssembler.toDto(activeDriversService.getActiveDrivers());
  }

}
