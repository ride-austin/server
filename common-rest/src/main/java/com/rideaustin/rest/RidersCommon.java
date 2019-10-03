package com.rideaustin.rest;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;

import org.apache.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rideaustin.CheckedTransactional;
import com.rideaustin.RiderEndpoint;
import com.rideaustin.WebClientEndpoint;
import com.rideaustin.assemblers.RiderDtoEnricher;
import com.rideaustin.model.Charity;
import com.rideaustin.model.enums.AvatarType;
import com.rideaustin.model.user.Rider;
import com.rideaustin.model.user.User;
import com.rideaustin.rest.exception.RideAustinException;
import com.rideaustin.rest.model.RiderDto;
import com.rideaustin.service.user.RiderService;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;

@RestController
@CheckedTransactional
@RequestMapping("/rest/riders")
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class RidersCommon {

  private final RiderService riderService;
  private final RiderDtoEnricher riderAssembler;

  @RiderEndpoint
  @WebClientEndpoint
  @GetMapping("/{id}")
  @RolesAllowed({AvatarType.ROLE_ADMIN, AvatarType.ROLE_RIDER})
  @ApiOperation("Get rider information as a rider or an administrator")
  @ApiResponses({
    @ApiResponse(code = HttpStatus.SC_OK, message = "OK"),
    @ApiResponse(code = HttpStatus.SC_NOT_FOUND, message = "Rider not found")
  })
  public RiderDto getRider(
    @ApiParam(value = "Rider ID", example = "1") @PathVariable long id
  ) throws RideAustinException {
    RiderDto rider = riderService.findRiderInfo(id);
    return riderAssembler.toDto(rider);
  }

  @RiderEndpoint
  @WebClientEndpoint
  @PutMapping("/{id}")
  @RolesAllowed({AvatarType.ROLE_ADMIN, AvatarType.ROLE_RIDER})
  @ApiOperation("Update rider profile as a rider or an administrator")
  @ApiResponses({
    @ApiResponse(code = HttpStatus.SC_OK, message = "OK"),
    @ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = "Provided data is invalid"),
    @ApiResponse(code = HttpStatus.SC_NOT_FOUND, message = "Rider not found")
  })
  public RiderDto updateRider(
    @ApiParam(value = "Rider ID", example = "1") @PathVariable long id,
    @ApiParam("Rider object") @RequestBody RiderDto riderDTO
  ) throws RideAustinException {
    Rider rider = createRider(riderDTO);
    boolean deviceBlocked = riderDTO.getUser().isDeviceBlocked();
    boolean active = riderDTO.isActive();
    riderService.updateRider(id, rider, deviceBlocked, active);
    return riderService.getCurrentRider();
  }

  private Rider createRider(RiderDto riderDto) {
    Rider rider = new Rider();
    rider.setActive(riderDto.isActive());
    User user = User.builder()
      .email(riderDto.getUser().getEmail())
      .firstname(riderDto.getUser().getFirstname())
      .lastname(riderDto.getUser().getLastname())
      .dateOfBirth(riderDto.getUser().getDateOfBirth())
      .address(riderDto.getUser().getAddress())
      .facebookId(riderDto.getUser().getFacebookId())
      .gender(riderDto.getUser().getGender())
      .phoneNumber(riderDto.getUser().getPhoneNumber())
      .userEnabled(riderDto.getUser().isEnabled())
      .build();
    user.setId(riderDto.getUser().getId());
    rider.setUser(user);
    rider.setId(riderDto.getId());
    if (riderDto.getCharity() != null) {
      rider.setCharity(
        Charity.builder()
          .id(riderDto.getCharity().getId())
          .build()
      );
    }
    return rider;
  }

}
