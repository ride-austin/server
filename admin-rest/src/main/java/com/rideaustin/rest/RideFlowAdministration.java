package com.rideaustin.rest;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;

import org.apache.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rideaustin.WebClientEndpoint;
import com.rideaustin.model.enums.AvatarType;
import com.rideaustin.service.RideFlowService;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/rest/rides")
@RolesAllowed(AvatarType.ROLE_ADMIN)
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class RideFlowAdministration {

  private final RideFlowService rideFlowService;

  @WebClientEndpoint
  @ApiOperation("Cancel the ride as an administrator")
  @DeleteMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE, params = AvatarType.MAPPING_ADMIN)
  @ApiResponses({
    @ApiResponse(code = HttpStatus.SC_OK, message = "OK"),
    @ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = "Failed to process request")
  })
  public void cancelRideAsAdmin(
    @ApiParam(value = "Ride ID", example = "1") @PathVariable long id
  ) {
    rideFlowService.cancelAsAdmin(id);
  }
}
