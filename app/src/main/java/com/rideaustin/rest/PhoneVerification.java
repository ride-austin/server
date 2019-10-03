package com.rideaustin.rest;

import javax.inject.Inject;

import org.apache.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.rideaustin.CheckedTransactional;
import com.rideaustin.MobileClientEndpoint;
import com.rideaustin.rest.exception.BadRequestException;
import com.rideaustin.rest.exception.RideAustinException;
import com.rideaustin.rest.model.AuthenticationToken;
import com.rideaustin.service.user.PhoneVerificationService;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;

@RestController
@CheckedTransactional
@RequestMapping("/rest/phoneVerification")
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class PhoneVerification {

  private final PhoneVerificationService phoneVerificationService;

  @MobileClientEndpoint
  @PostMapping("/requestCode")
  @ApiOperation(value = "Request a code to be sent to user's phone", response = AuthenticationToken.class)
  @ApiResponses({
    @ApiResponse(code = HttpStatus.SC_OK, message = "OK"),
    @ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = "Phone number is invalid or failure to send a message")
  })
  public ResponseEntity<AuthenticationToken> requestCode(
    @ApiParam(value = "Phone number", example = "+15125555555", required = true) @RequestParam String phoneNumber
  ) throws RideAustinException {
    AuthenticationToken singleRequestAuthenticationToken = phoneVerificationService.initiate(phoneNumber);
    return ResponseEntity.ok(singleRequestAuthenticationToken);
  }

  @MobileClientEndpoint
  @PostMapping("/verify")
  @ApiOperation("Verify received code")
  @ApiResponses({
    @ApiResponse(code = HttpStatus.SC_OK, message = "OK"),
    @ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = "SMS verification failed")
  })
  public void verify(
    @ApiParam(value = "Token received as a response to /requestCode call", required = true) @RequestParam String authToken,
    @ApiParam(value = "Code received in SMS", required = true) @RequestParam String code
  ) throws BadRequestException {
    Boolean verified = phoneVerificationService.verify(authToken, code);
    if (verified) {
      throw new BadRequestException("SMS verification failed");
    }
  }
}
