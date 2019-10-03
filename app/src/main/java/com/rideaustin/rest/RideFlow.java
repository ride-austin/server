package com.rideaustin.rest;

import java.math.BigDecimal;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import org.apache.http.HttpStatus;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;

import com.rideaustin.DriverEndpoint;
import com.rideaustin.RiderEndpoint;
import com.rideaustin.assemblers.MobileDriverRideDtoEnricher;
import com.rideaustin.assemblers.MobileRiderRideDtoEnricher;
import com.rideaustin.model.enums.AvatarType;
import com.rideaustin.model.enums.CancellationReason;
import com.rideaustin.model.enums.PaymentProvider;
import com.rideaustin.model.user.User;
import com.rideaustin.repo.dsl.RideDslRepository;
import com.rideaustin.rest.exception.BadRequestException;
import com.rideaustin.rest.exception.ForbiddenException;
import com.rideaustin.rest.exception.RideAustinException;
import com.rideaustin.rest.model.MobileDriverRideDto;
import com.rideaustin.rest.model.MobileRiderRideDto;
import com.rideaustin.rest.model.RideEndLocation;
import com.rideaustin.rest.model.RideRequestParams;
import com.rideaustin.rest.model.RideStartLocation;
import com.rideaustin.service.CurrentUserService;
import com.rideaustin.service.RideFlowService;
import com.rideaustin.service.RideRatingService;
import com.rideaustin.service.RideRequestingService;
import com.rideaustin.service.ride.RideOwnerService;
import com.rideaustin.utils.CommentUtils;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Validated
@RestController
@RequestMapping("/rest/rides")
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class RideFlow {

  private final RideFlowService rideFlowService;
  private final RideOwnerService rideOwnerService;
  private final RideRatingService rideRatingService;
  private final CurrentUserService currentUserService;
  private final RideRequestingService requestingService;

  private final RideDslRepository rideDslRepository;

  private final MobileDriverRideDtoEnricher mobileDriverRideDtoEnricher;
  private final MobileRiderRideDtoEnricher<MobileRiderRideDto> mobileRiderRideDtoEnricher;

  private final Environment environment;

  @RiderEndpoint
  @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  @RolesAllowed({AvatarType.ROLE_RIDER, AvatarType.ROLE_API_CLIENT})
  @ApiOperation(value = "Request a ride", notes = "Request a ride from startLocation to endLocation with given car category",
  response = MobileRiderRideDto.class)
  @ApiResponses({
    @ApiResponse(code = HttpStatus.SC_OK, message = "OK"),
    @ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = "Provided parameters are invalid"),
    @ApiResponse(code = HttpStatus.SC_PAYMENT_REQUIRED, message = "Requesting user has an outstanding balance to be paid"),
    @ApiResponse(code = HttpStatus.SC_FORBIDDEN, message = "Requesting user is not a rider or api client"),
    @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = "Server is misconfigured")
  })
  public ResponseEntity requestRide(
    @ApiParam @Valid @ModelAttribute RideStartLocation startLocation,
    @ApiParam @ModelAttribute RideEndLocation endLocation,
    @ApiParam(value = "Car category", allowableValues = "REGULAR,SUV,PREMIUM", defaultValue = "REGULAR") @RequestParam(defaultValue = "REGULAR", required = false) String carCategory,
    @ApiParam(value = "Is request originating in surge area", required = true, defaultValue = "false") @RequestParam(defaultValue = "false") Boolean inSurgeArea,
    @ApiParam(value = "City ID", example = "1", required = true, defaultValue = "1") @RequestParam(defaultValue = "1") Long cityId,
    @ApiParam @ModelAttribute RideRequestParams params
  ) throws RideAustinException {
    final User user = currentUserService.getUser();
    if (user.isRider() && !user.isApiClient()) {
      return ResponseEntity.ok(requestingService.requestRideAsRider(startLocation, endLocation, carCategory, inSurgeArea, cityId, params));
    } else if (user.isApiClient() && AvatarType.API_CLIENT.equals(params.getAvatarType())) {
      return ResponseEntity.ok(requestingService.requestRideAsApiClient(startLocation, endLocation, carCategory, cityId));
    }
    throw new ForbiddenException("You are not permitted to request a ride");
  }

  @DriverEndpoint
  @RolesAllowed(AvatarType.ROLE_DRIVER)
  @PostMapping(value = "/{id}/received", produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiOperation(value = "Acknowledge handshake received by driver app for a ride", response = Object.class)
  @ApiResponses({
    @ApiResponse(code = HttpStatus.SC_OK, message = "OK"),
    @ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = "Failed to process request")
  })
  public DeferredResult<ResponseEntity<Object>> acknowledgeHandshake(
    @ApiParam(value = "Ride ID", example = "1") @PathVariable long id
  ) {
    DeferredResult<ResponseEntity<Object>> result = new DeferredResult<>(50L, ResponseEntity.ok().build());
    rideFlowService.acknowledgeHandshake(id, result);
    return result;
  }

  @DriverEndpoint
  @RolesAllowed(AvatarType.ROLE_DRIVER)
  @ApiOperation(value = "Accept ride request", response = Object.class)
  @PostMapping(value = "/{id}/accept", produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponses({
    @ApiResponse(code = HttpStatus.SC_OK, message = "OK"),
    @ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = "Failed to process request")
  })
  public DeferredResult<ResponseEntity<Object>> acceptRide(
    @ApiParam(value = "Ride ID", example = "1") @PathVariable long id
  ) {
    DeferredResult<ResponseEntity<Object>> result = new DeferredResult<>(getTimeout(), ResponseEntity.ok().build());
    rideFlowService.acceptRide(id, result);
    return result;
  }

  @DriverEndpoint
  @RolesAllowed(AvatarType.ROLE_DRIVER)
  @ApiOperation(value = "Decline ride request", response = Object.class)
  @DeleteMapping(value = "/{id}/decline", produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponses({
    @ApiResponse(code = HttpStatus.SC_OK, message = "OK"),
    @ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = "Failed to process request")
  })
  public ResponseEntity<Object> declineRide(
    @ApiParam(value = "Ride ID", example = "1") @PathVariable Long id
  ) {
    rideFlowService.declineRide(id);
    return ResponseEntity.ok().build();
  }

  @DriverEndpoint
  @RolesAllowed(AvatarType.ROLE_DRIVER)
  @PostMapping(value = "/{id}/reached", produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiOperation(value = "Mark ride request as DRIVER_REACHED", response = Object.class)
  @ApiResponses({
    @ApiResponse(code = HttpStatus.SC_OK, message = "OK"),
    @ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = "Failed to process request")
  })
  public DeferredResult<ResponseEntity<Object>> driverReached(
    @ApiParam(value = "Ride ID", example = "1") @PathVariable long id
  ) {
    DeferredResult<ResponseEntity<Object>> result = new DeferredResult<>(getTimeout(), ResponseEntity.ok().build());
    rideFlowService.driverReached(id, result);
    return result;
  }

  @DriverEndpoint
  @RolesAllowed(AvatarType.ROLE_DRIVER)
  @ApiOperation(value = "Start the ride", response = Object.class)
  @PostMapping(value = "/{id}/start", produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponses({
    @ApiResponse(code = HttpStatus.SC_OK, message = "OK"),
    @ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = "Failed to process request")
  })
  public DeferredResult<ResponseEntity<Object>> startRide(
    @ApiParam(value = "Ride ID", example = "1") @PathVariable long id
  ) {
    DeferredResult<ResponseEntity<Object>> result = new DeferredResult<>(getTimeout(), ResponseEntity.ok().build());
    rideFlowService.startRide(id, result);
    return result;
  }

  @RiderEndpoint
  @RolesAllowed({AvatarType.ROLE_RIDER})
  @ApiOperation(value = "Update ride destination and/or comment", response = Object.class)
  @PutMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponses({
    @ApiResponse(code = HttpStatus.SC_OK, message = "OK"),
    @ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = "Failed to process request")
  })
  public DeferredResult<ResponseEntity> updateRideDestination(
    @ApiParam(value = "Ride ID", example = "1") @PathVariable long id,
    @ApiParam @ModelAttribute RideEndLocation endLocation,
    @ApiParam("New ride comment") @RequestParam(required = false) String comment
  ) throws RideAustinException {
    DeferredResult<ResponseEntity> result = new DeferredResult<>(getTimeout(), ResponseEntity.ok().build());
    if (endLocation.getLat() != null && endLocation.getLng() != null) {
      rideFlowService.updateDestination(id, endLocation, result);
    }
    if (comment != null) {
      CommentUtils.validateComment(comment);
      rideFlowService.updateComment(id, comment);
      result.setResult(ResponseEntity.ok().build());
    }
    return result;
  }

  @DriverEndpoint
  @RolesAllowed(AvatarType.ROLE_DRIVER)
  @ApiOperation(value = "End the ride", response = MobileDriverRideDto.class)
  @PostMapping(value = "/{id}/end", produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponses({
    @ApiResponse(code = HttpStatus.SC_OK, message = "OK"),
    @ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = "Failed to process request")
  })
  public DeferredResult<MobileDriverRideDto> endRide(
    @ApiParam(value = "Ride ID", example = "1") @PathVariable long id,
    @ApiParam @ModelAttribute RideEndLocation endLocation
  ) {
    DeferredResult<MobileDriverRideDto> result = new DeferredResult<>(getTimeout(), mobileDriverRideDtoEnricher.enrich(rideDslRepository.findOneForDriver(id)));
    rideFlowService.endRide(id, endLocation, result);
    return result;
  }

  @DriverEndpoint
  @RolesAllowed(AvatarType.ROLE_DRIVER)
  @ApiOperation(value = "Cancel ride as a driver", response = Object.class)
  @DeleteMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE, params = AvatarType.MAPPING_DRIVER)
  @ApiResponses({
    @ApiResponse(code = HttpStatus.SC_OK, message = "OK"),
    @ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = "Failed to process request")
  })
  public DeferredResult<ResponseEntity<Object>> cancelRideAsDriver(
    @ApiParam(value = "Ride ID", example = "1") @PathVariable long id,
    @ApiParam(value = "Cancellation reason code", allowableValues = "NO_SHOW,WRONG_GPS,TOO_MANY_RIDERS") @RequestParam(required = false) CancellationReason reason,
    @ApiParam("Cancellation comment") @RequestParam(required = false) String comment
  ) {
    DeferredResult<ResponseEntity<Object>> result = new DeferredResult<>(getTimeout(), ResponseEntity.ok().build());
    rideFlowService.cancelAsDriver(id, result, reason, comment);
    return result;
  }

  @RiderEndpoint
  @RolesAllowed(AvatarType.ROLE_RIDER)
  @ApiOperation(value = "Cancel ride as a rider", response = Object.class)
  @DeleteMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE, params = AvatarType.MAPPING_RIDER)
  @ApiResponses({
    @ApiResponse(code = HttpStatus.SC_OK, message = "OK"),
    @ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = "Failed to process request")
  })
  public DeferredResult<ResponseEntity<Object>> cancelRideAsRider(
    @ApiParam(value = "Ride ID", example = "1") @PathVariable long id
  ) {
    DeferredResult<ResponseEntity<Object>> result = new DeferredResult<>(getTimeout(), ResponseEntity.ok().build());
    rideFlowService.cancelAsRider(id, result);
    return result;
  }

  @RiderEndpoint
  @RolesAllowed(AvatarType.ROLE_RIDER)
  @ApiOperation(value = "Cancel ride as a dispatcher", response = Object.class)
  @DeleteMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE, params = AvatarType.MAPPING_DISPATCHER)
  @ApiResponses({
    @ApiResponse(code = HttpStatus.SC_OK, message = "OK"),
    @ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = "Failed to process request")
  })
  public DeferredResult<ResponseEntity<Object>> cancelRideAsDispatcher(
    @ApiParam(value = "Ride ID", example = "1") @PathVariable long id
  ) {
    return cancelRideAsRider(id);
  }

  @RiderEndpoint
  @RolesAllowed({AvatarType.ROLE_RIDER, AvatarType.ROLE_DRIVER})
  @ApiOperation("Submit ride rating and/or tip, and/or comment")
  @PutMapping(value = "/{id}/rating", produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponses({
    @ApiResponse(code = HttpStatus.SC_OK, message = "OK", response = MobileRiderRideDto.class),
    @ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = "Failed to process request")
  })
  public ResponseEntity rateRide(
    @ApiParam(value = "Ride ID", example = "1") @PathVariable long id,
    @ApiParam(value = "Rating value", example = "5.0", required = true)
    @RequestParam @Valid @NotNull(message = "Please enter the rating")
    @Min(value = 1, message = "Please enter correct rating")
    @Max(value = 5, message = "Please enter correct rating") BigDecimal rating,
    @ApiParam(value = "Ride tip amount", example = "3.0") @RequestParam(required = false) BigDecimal tip,
    @ApiParam("Ride completion comment") @RequestParam(required = false) String comment,
    @ApiParam("Payment provider used to pay for ride") @RequestParam(required = false) PaymentProvider paymentProvider
  ) throws RideAustinException {
    if (rideOwnerService.isDriversRide(id)) {
      rideRatingService.rateRideAsDriver(id, rating, comment);
      return ResponseEntity.ok(rideDslRepository.findOneForDriver(id));
    } else if (rideOwnerService.isRideRider(id)){
      rideRatingService.rateRideAsRider(id, rating, tip, comment);
      final MobileRiderRideDto response = rideDslRepository.findRiderRideInfo(id);
      response.setPaymentProvider(paymentProvider);
      return ResponseEntity.ok(mobileRiderRideDtoEnricher.enrich(response));
    } else {
      throw new BadRequestException("You can not rate this ride");
    }
  }

  @PostMapping("queue/{token}")
  @RolesAllowed(AvatarType.ROLE_RIDER)
  @ApiOperation("Associate a ride with a queued token as a rider")
  @ApiResponses({
    @ApiResponse(code = HttpStatus.SC_OK, message = "OK"),
    @ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = "Ride token expired"),
    @ApiResponse(code = HttpStatus.SC_NOT_FOUND, message = "Ride token not found")
  })
  public MobileRiderRideDto associateRide(
    @ApiParam("Queued ride token") @PathVariable String token
  ) throws RideAustinException {
    return rideFlowService.associateRide(token);
  }

  private long getTimeout() {
    return environment.getProperty("rideflow.defer.timeout", Long.class, 25000L);
  }
}
