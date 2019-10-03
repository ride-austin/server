package com.rideaustin.rest;

import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rideaustin.model.enums.AvatarType;
import com.rideaustin.rest.model.FarePaymentDto;
import com.rideaustin.service.farepayment.FarePaymentService;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/rest/farepayments")
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class FarePayments {

  private final FarePaymentService farePaymentService;

  @ApiOperation("List payments performed for a ride")
  @RolesAllowed({AvatarType.ROLE_ADMIN, AvatarType.ROLE_RIDER})
  @GetMapping(value = "/{rideId}/list", produces = MediaType.APPLICATION_JSON_VALUE)
  public List<FarePaymentDto> listForRide(
    @ApiParam(value = "Ride ID", example = "1") @PathVariable("rideId") Long rideId
  ) {
    return farePaymentService.getAcceptedPaymentParticipantsInfo(rideId);
  }

}
