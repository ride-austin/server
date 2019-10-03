package com.rideaustin.rest;

import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;

import org.apache.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.rideaustin.RiderEndpoint;
import com.rideaustin.model.CancellationReasonDto;
import com.rideaustin.model.enums.AvatarType;
import com.rideaustin.model.enums.CancellationReason;
import com.rideaustin.rest.exception.BadRequestException;
import com.rideaustin.service.CancellationFeedbackService;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/rest/rides/cancellation")
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class RideCancellationFeedback {

  private final CancellationFeedbackService cancellationFeedbackService;

  @RiderEndpoint
  @PostMapping("/{rideId}")
  @RolesAllowed(AvatarType.ROLE_RIDER)
  @ApiOperation("Submit feedback as a rider after cancelling the ride")
  @ApiResponses({
    @ApiResponse(code = HttpStatus.SC_OK, message = "OK"),
    @ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = "Feedback can't be submitted")
  })
  public void submitCancellationFeedback(
    @ApiParam(value = "Ride ID", example = "1") @PathVariable long rideId,
    @ApiParam(value = "Cancellation reason code", required = true, allowableValues = "CHANGE_BOOKING,CHANGE_MIND,ANOTHER_RIDE,MISTAKE,TOO_LONG")
    @RequestParam CancellationReason reason,
    @ApiParam("Cancellation comment") @RequestParam(required = false) String comment
  ) throws BadRequestException {
    cancellationFeedbackService.submit(rideId, reason, AvatarType.RIDER, comment);
  }

  @GetMapping
  @RiderEndpoint
  @ApiOperation("List available cancellation reasons")
  @RolesAllowed({AvatarType.ROLE_RIDER, AvatarType.ROLE_DRIVER})
  @ApiResponses({
    @ApiResponse(code = HttpStatus.SC_OK, message = "OK")
  })
  public List<CancellationReasonDto> listCancellationReasons(
    @ApiParam(value = "City ID", example = "1", defaultValue = "1") @RequestParam(defaultValue = "1") long cityId,
    @ApiParam(value = "Avatar type", allowableValues = "RIDER,DRIVER", defaultValue = "RIDER") @RequestParam(defaultValue = "RIDER") AvatarType avatarType
  ) {
    return cancellationFeedbackService.listReasons(cityId, avatarType);
  }
}
