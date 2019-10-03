package com.rideaustin.rest;

import static com.rideaustin.Constants.ErrorMessages.DEVICE_IS_BLOCKED;
import static com.rideaustin.utils.FraudLogUtil.extractIPAddress;
import static com.rideaustin.utils.FraudLogUtil.fraudLog;

import javax.inject.Inject;
import javax.persistence.LockTimeoutException;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.exception.LockAcquisitionException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.rideaustin.CheckedTransactional;
import com.rideaustin.ExternalEndpoint;
import com.rideaustin.MobileClientEndpoint;
import com.rideaustin.WebClientEndpoint;
import com.rideaustin.filter.ClientAppVersionContext;
import com.rideaustin.model.Session;
import com.rideaustin.model.enums.ApiClientAppType;
import com.rideaustin.model.user.User;
import com.rideaustin.repo.dsl.UserDslRepository;
import com.rideaustin.repo.jpa.UserRepository;
import com.rideaustin.rest.exception.BadRequestException;
import com.rideaustin.rest.exception.ForbiddenException;
import com.rideaustin.rest.exception.RideAustinException;
import com.rideaustin.rest.exception.ServerError;
import com.rideaustin.rest.exception.UnAuthorizedException;
import com.rideaustin.rest.model.AuthenticationToken;
import com.rideaustin.service.CurrentUserService;
import com.rideaustin.service.SessionService;
import com.rideaustin.service.UserService;
import com.rideaustin.service.model.FacebookUser;
import com.rideaustin.service.thirdparty.FacebookService;
import com.rideaustin.service.user.BlockedDeviceService;
import com.rideaustin.utils.CryptUtils;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import springfox.documentation.annotations.ApiIgnore;

@Slf4j
@RestController
@CheckedTransactional
@RequestMapping("/rest")
@RequiredArgsConstructor(onConstructor = @__(@Inject))
@Api(tags = "authentication")
public class Rest {

  private final CurrentUserService cuSvc;
  private final UserRepository userRepository;
  private final UserDslRepository userDslRepository;
  private final SessionService sessionService;
  private final PasswordEncoder encoder;
  private final UserService userService;
  private final FacebookService facebookService;
  private final BlockedDeviceService blockedDeviceService;
  private final CryptUtils cryptUtils;

  @MobileClientEndpoint
  @PostMapping("/password")
  @ApiOperation("Change password as an user")
  @ApiResponses({
    @ApiResponse(code = org.apache.http.HttpStatus.SC_OK, message = "OK")
  })
  public void changePassword(
    @ApiParam(value = "New password", required = true) @RequestParam String password
  ) {
    changePassword(password, cuSvc.getUser());
  }

  @WebClientEndpoint
  @MobileClientEndpoint
  @PostMapping("/login")
  @ApiOperation(value = "Authenticate as an user", response = AuthenticationToken.class)
  @ApiResponses({
    @ApiResponse(code = org.apache.http.HttpStatus.SC_OK, message = "OK"),
    @ApiResponse(code = org.apache.http.HttpStatus.SC_UNAUTHORIZED, message = "Credentials are incorrect"),
    @ApiResponse(code = org.apache.http.HttpStatus.SC_FORBIDDEN, message = "Device blocked"),
    @ApiResponse(code = org.apache.http.HttpStatus.SC_INTERNAL_SERVER_ERROR, message = "Failed to login")
  })
  public ResponseEntity<AuthenticationToken> login(HttpServletRequest request)
    throws RideAustinException {
    User user = cuSvc.getUser();
    if (user != null) {

      try {
        user = userRepository.findOneForUpdate(user.getId());

        Session session = sessionService.createNewSession(request, user);

        if (blockedDeviceService.isInBlocklist(session.getUserDeviceId())) {
          fraudLog(log, String.format("Login attempt from blocked device %s, email %s, ip %s", session.getUserDeviceId(),
            user.getEmail(), extractIPAddress(request)));
          throw new ForbiddenException(DEVICE_IS_BLOCKED);
        }

        AuthenticationToken token = new AuthenticationToken();
        token.setToken(session.getAuthToken());

        return new ResponseEntity<>(token, HttpStatus.OK);
      } catch (LockAcquisitionException | LockTimeoutException e) {
        log.warn("Unable to acquire lock on user entity ", e);
        throw new ServerError("Unable to sign in. Please try again");
      }
    }
    throw new UnAuthorizedException("Your email or password is not correct. Please try again!");
  }

  @WebClientEndpoint
  @MobileClientEndpoint
  @PostMapping("/logout")
  @ApiOperation("Log out")
  @ApiResponses({
    @ApiResponse(code = org.apache.http.HttpStatus.SC_OK, message = "OK"),
    @ApiResponse(code = org.apache.http.HttpStatus.SC_BAD_REQUEST, message = "Logging out is forbidden while in a ride")
  })
  public void logout() throws BadRequestException {
    User user = cuSvc.getUser();
    if (user == null) {
      return;
    }
    userService.logOutUser(user.getId(),
      ApiClientAppType.getSuggestedAvatarTypeFromHeader(ClientAppVersionContext.getAppVersion().getUserAgent()));
  }

  @ApiIgnore
  @MobileClientEndpoint
  @PostMapping(value = "/facebook", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
  public ResponseEntity getFacebook(@RequestParam String token) throws UnAuthorizedException {
    FacebookUser fbUser = facebookService.getFacebookUser(token);
    if (fbUser == null) {
      throw new UnAuthorizedException();
    }
    User user = userDslRepository.findAnyByEmail(fbUser.getEmail());
    if (user == null) {
      return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }
    user.setFacebookId(fbUser.getId());
    if (StringUtils.isEmpty(user.getPhotoUrl())) {
      user.setPhotoUrl(facebookService.getPhotoUrl(fbUser));
    }
    userRepository.saveAndFlush(user);
    String passwordHash = cryptUtils.clientAppHash(user.getEmail(), token);
    user.setPassword(encoder.encode(passwordHash));
    return ResponseEntity.status(HttpStatus.OK).build();
  }

  @ApiIgnore
  @ExternalEndpoint
  @GetMapping("/health")
  @ResponseStatus(HttpStatus.OK)
  public void healthCheck() {
    // Do nothing
  }

  private void changePassword(String newPassword, User user) {
    userDslRepository.changePassword(user.getId(), encoder.encode(newPassword));
  }

}
