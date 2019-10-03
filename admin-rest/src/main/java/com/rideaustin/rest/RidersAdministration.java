package com.rideaustin.rest;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;

import org.apache.http.HttpStatus;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rideaustin.WebClientEndpoint;
import com.rideaustin.model.enums.AvatarType;
import com.rideaustin.rest.exception.ServerError;
import com.rideaustin.rest.model.ListRidersParams;
import com.rideaustin.rest.model.PagingParams;
import com.rideaustin.rest.model.SimpleRiderDto;
import com.rideaustin.service.RiderCardService;
import com.rideaustin.service.user.RiderAdministrationService;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/rest/riders")
@RolesAllowed(AvatarType.ROLE_ADMIN)
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class RidersAdministration {

  private final RiderAdministrationService riderService;
  private final RiderCardService cardService;

  @GetMapping("/list")
  @ApiOperation(value = "Get a paginated list of rider information", response = SimpleRiderDto.class, responseContainer = "List")
  public Page<SimpleRiderDto> listRidersDto(
    @ApiParam @ModelAttribute ListRidersParams params,
    @ApiParam @ModelAttribute PagingParams paging
  ) {
    return riderService.listRidersToDto(params, paging);
  }

  @WebClientEndpoint
  @PostMapping("/export")
  @ApiOperation("Export rider list")
  @ApiResponses({
    @ApiResponse(code = HttpStatus.SC_OK, message = "OK"),
    @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = "Failed to trigger report job")
  })
  public void exportRiders(@ApiParam @ModelAttribute ListRidersParams params) throws ServerError {
    riderService.exportRiders(params);
  }

  @WebClientEndpoint
  @PostMapping("/{id}/unlock")
  @ApiOperation("Unlock rider's cards")
  @ApiResponses({
    @ApiResponse(code = HttpStatus.SC_OK, message = "OK"),
  })
  public void unlock(
    @ApiParam(value = "Rider ID", example = "1") @PathVariable long id
  ) {
    cardService.unlock(id);
  }
}
