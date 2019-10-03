package com.rideaustin.rest;

import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;

import org.apache.http.HttpStatus;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.rideaustin.CheckedTransactional;
import com.rideaustin.RiderEndpoint;
import com.rideaustin.assemblers.PaymentHistoryDtoAssembler;
import com.rideaustin.model.enums.AvatarType;
import com.rideaustin.model.enums.RideStatus;
import com.rideaustin.model.splitfare.FarePayment;
import com.rideaustin.model.user.Rider;
import com.rideaustin.model.user.User;
import com.rideaustin.rest.exception.BadRequestException;
import com.rideaustin.rest.exception.ForbiddenException;
import com.rideaustin.rest.exception.RideAustinException;
import com.rideaustin.rest.model.PagingParams;
import com.rideaustin.rest.model.PaymentHistoryDto;
import com.rideaustin.service.CurrentUserService;
import com.rideaustin.service.farepayment.FarePaymentService;
import com.rideaustin.service.model.PendingPaymentDto;
import com.rideaustin.service.payment.PaymentService;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;

@RestController
@CheckedTransactional
@RequestMapping("/rest/riders/{id}/payments")
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class RiderPayments {

  private final CurrentUserService currentUserService;
  private final FarePaymentService farePaymentService;
  private final PaymentService paymentService;
  private final PaymentHistoryDtoAssembler paymentHistoryDtoAssembler;

  @GetMapping
  @RiderEndpoint
  @RolesAllowed({AvatarType.ROLE_ADMIN, AvatarType.ROLE_RIDER})
  @ApiOperation(value = "Get payments history as a rider or administrator", response = PaymentHistoryDto.class, responseContainer = "List")
  @ApiResponses({
    @ApiResponse(code = HttpStatus.SC_OK, message = "OK")
  })
  public Page<PaymentHistoryDto> paymentHistory(
    @ApiParam(value = "Rider ID", example = "1") @PathVariable long id,
    @ApiParam("Ride status") @RequestParam(value = "status", required = false) RideStatus status,
    @ApiParam @ModelAttribute PagingParams pagingParams
  ) {
    Page<FarePayment> riderPaymentHistory = farePaymentService.getRiderPaymentHistory(id, status, pagingParams);
    return riderPaymentHistory.map(paymentHistoryDtoAssembler);
  }

  @RiderEndpoint
  @GetMapping("/pending")
  @ApiOperation("Get a list of pending payments")
  @RolesAllowed({AvatarType.ROLE_ADMIN, AvatarType.ROLE_RIDER})
  @ApiResponses({
    @ApiResponse(code = HttpStatus.SC_OK, message = "OK"),
    @ApiResponse(code = HttpStatus.SC_FORBIDDEN, message = "User is not permitted to perform the request")
  })
  public List<PendingPaymentDto> pendingPayments(
    @ApiParam(value = "Rider ID", example = "1") @PathVariable long id
  ) throws ForbiddenException {
    User user = currentUserService.getUser();
    if (!user.isAdmin() && user.getAvatar(Rider.class).getId() != id) {
      throw new ForbiddenException();
    }
    return farePaymentService.listPendingPayments(id);
  }

  @RiderEndpoint
  @PostMapping("/pending")
  @RolesAllowed({AvatarType.ROLE_ADMIN, AvatarType.ROLE_RIDER})
  @ApiOperation("Process pending payment, pay outstanding balance as a rider")
  @ApiResponses({
    @ApiResponse(code = HttpStatus.SC_OK, message = "OK"),
    @ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = "Failed to process payment"),
    @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = "Failed to process payment")
  })
  public void processPendingPayment(
    @ApiParam(value = "Rider ID", example = "1") @PathVariable long id,
    @ApiParam(value = "Ride ID", example = "1") @RequestParam long rideId,
    @ApiParam("Apple pay token") @RequestParam(required = false) String applePayToken
  ) throws RideAustinException {
    boolean result = paymentService.processRidePayment(rideId, applePayToken);
    if (!result) {
      throw new BadRequestException("Failed to process pending payment");
    }
  }
}
