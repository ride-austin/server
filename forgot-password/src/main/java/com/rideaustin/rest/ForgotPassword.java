package com.rideaustin.rest;

import java.util.Optional;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.amazonaws.services.kms.model.NotFoundException;
import com.rideaustin.ExternalEndpoint;
import com.rideaustin.WebClientEndpoint;
import com.rideaustin.model.enums.AvatarType;
import com.rideaustin.model.user.User;
import com.rideaustin.repo.dsl.UserDslRepository;
import com.rideaustin.rest.exception.RideAustinException;
import com.rideaustin.service.ForgotPasswordService;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class ForgotPassword {

  private final Environment environment;
  private final UserDslRepository userDslRepository;
  private final ForgotPasswordService forgotPasswordService;

  @WebClientEndpoint
  @PostMapping("/rest/forgot")
  @ApiOperation("Request a password reminder email")
  @ApiResponses({
    @ApiResponse(code = HttpStatus.SC_OK, message = "OK"),
    @ApiResponse(code = HttpStatus.SC_NOT_FOUND, message = "User not found"),
    @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = "Failed to send email")
  })
  public void forgotPassword(
    @ApiParam(value = "User email to reset a password", required = true) @RequestParam String email
  ) throws RideAustinException {
    User user = Optional.ofNullable(userDslRepository.findByEmail(email.trim()))
      .orElseThrow(() -> new NotFoundException("User not found"));
    forgotPasswordService.sendPasswordReminderEmail(user);
  }

  @WebClientEndpoint
  @RolesAllowed(AvatarType.ROLE_ADMIN)
  @ApiOperation("Reset a password as an admin")
  @PostMapping(value = "/rest/forgot", params = AvatarType.MAPPING_ADMIN)
  @ApiResponses({
    @ApiResponse(code = HttpStatus.SC_OK, message = "OK"),
    @ApiResponse(code = HttpStatus.SC_NOT_FOUND, message = "User not found"),
    @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = "Failed to send email")
  })
  public void forceResetPassword(
    @ApiParam(value = "User email to reset a password", required = true) @RequestParam String email
  ) throws RideAustinException {
    User user = Optional.ofNullable(userDslRepository.findByEmail(email.trim()))
      .orElseThrow(() -> new NotFoundException("User not found"));
    forgotPasswordService.forceResetPassword(user);
  }

  @ExternalEndpoint
  @GetMapping("/password-reset")
  @ApiOperation("Endpoint to be called externally when an user gets reset email")
  @ApiResponses({
    @ApiResponse(code = HttpStatus.SC_MOVED_TEMPORARILY, message = "Redirect user to landing page")
  })
  public void resetPassword(@RequestParam String token, HttpServletResponse response) {
    boolean result = forgotPasswordService.resetPassword(token);
    response.setStatus(HttpStatus.SC_MOVED_TEMPORARILY);
    if (result) {
      response.setHeader(HttpHeaders.LOCATION, String.format("https://%s/password-reset", environment.getProperty("webapp.url")));
    } else {
      response.setHeader(HttpHeaders.LOCATION, String.format("https://%s/password-reset-failed", environment.getProperty("webapp.url")));
    }
  }
}
