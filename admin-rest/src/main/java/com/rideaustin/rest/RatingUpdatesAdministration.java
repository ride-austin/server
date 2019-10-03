package com.rideaustin.rest;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;

import org.apache.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.rideaustin.CheckedTransactional;
import com.rideaustin.WebClientEndpoint;
import com.rideaustin.model.enums.AvatarType;
import com.rideaustin.rest.exception.BadRequestException;
import com.rideaustin.rest.model.RatingUpdateDto;
import com.rideaustin.service.rating.RatingUpdateService;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;

@RestController
@CheckedTransactional
@RolesAllowed(AvatarType.ROLE_ADMIN)
@RequestMapping("/rest/ratingupdates")
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class RatingUpdatesAdministration {

  private final RatingUpdateService ratingUpdateService;

  @PatchMapping("/{id}/recalculate")
  @ApiOperation("Recalculate rating for an user")
  @ApiResponses({
    @ApiResponse(code = HttpStatus.SC_OK, message = "OK")
  })
  public void updateAll(
    @ApiParam(value = "Avatar ID", example = "1") @PathVariable long id,
    @ApiParam(value = "Avatar type", allowableValues = "RIDER,DRIVER", required = true) @RequestParam AvatarType type
  ) {
    this.ratingUpdateService.recalculate(id, type);
  }

  @WebClientEndpoint
  @DeleteMapping(value = "/{id}")
  @RolesAllowed(AvatarType.ROLE_ADMIN)
  @ApiOperation("Remove rating update")
  @ApiResponses({
    @ApiResponse(code = HttpStatus.SC_OK, message = "OK")
  })
  public void deleteRating(@ApiParam(value = "Rating update ID", example = "1") @PathVariable long id) {
    ratingUpdateService.deleteRating(id);
  }

  @WebClientEndpoint
  @PostMapping(value = "/{id}")
  @RolesAllowed(AvatarType.ROLE_ADMIN)
  @ApiOperation("Update rating update")
  @ApiResponses({
    @ApiResponse(code = HttpStatus.SC_OK, message = "OK")
  })
  public RatingUpdateDto updateRating(
    @ApiParam(value = "Rating update ID", example = "1") @PathVariable long id,
    @ApiParam(value = "New rating value", example = "5.0") @RequestParam double value
  ) throws BadRequestException {
    return ratingUpdateService.updateRating(id, value);
  }
}
