package com.rideaustin.rest;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;

import org.apache.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rideaustin.RiderEndpoint;
import com.rideaustin.assemblers.MobileRiderRideDtoEnricher;
import com.rideaustin.assemblers.RiderCardDtoAssembler;
import com.rideaustin.assemblers.RiderDtoEnricher;
import com.rideaustin.model.enums.AvatarType;
import com.rideaustin.rest.exception.RideAustinException;
import com.rideaustin.rest.model.CurrentRiderDto;
import com.rideaustin.rest.model.MobileRiderRideDto;
import com.rideaustin.rest.model.RiderDto;
import com.rideaustin.service.RideService;
import com.rideaustin.service.RiderCardService;
import com.rideaustin.service.farepayment.FarePaymentService;
import com.rideaustin.service.user.RiderService;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/rest/riders")
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class Riders {

  private final RiderService riderService;
  private final RideService rideService;
  private final RiderCardService riderCardService;
  private final FarePaymentService farePaymentService;
  private final MobileRiderRideDtoEnricher<MobileRiderRideDto> rideDtoEnricher;
  private final RiderCardDtoAssembler cardAssembler;
  private final RiderDtoEnricher riderAssembler;

  @RiderEndpoint
  @GetMapping("current")
  @RolesAllowed(AvatarType.ROLE_RIDER)
  @ApiOperation("Get current rider information")
  @ApiResponses({
    @ApiResponse(code = HttpStatus.SC_OK, message = "OK"),
    @ApiResponse(code = HttpStatus.SC_FORBIDDEN, message = "Current user is not a rider"),
    @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = "Error while getting rider cards information from Stripe")
  })
  public CurrentRiderDto current() throws RideAustinException {
    RiderDto currentRider = riderService.getCurrentRider();
    return CurrentRiderDto.builder()
      .ride(rideDtoEnricher.enrich(rideService.getCurrentRideAsRider()))
      .rider(riderAssembler.toDto(currentRider))
      .cards(cardAssembler.toDto(riderCardService.listRiderCards(currentRider.getId())))
      .unpaid(farePaymentService.listPendingPayments(currentRider.getId()))
      .build();
  }

}