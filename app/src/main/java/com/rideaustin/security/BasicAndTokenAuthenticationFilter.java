package com.rideaustin.security;

import java.io.IOException;
import java.util.Optional;

import javax.inject.Inject;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.rideaustin.filter.ClientAppVersion;
import com.rideaustin.filter.ClientAppVersionFactory;
import com.rideaustin.model.Session;
import com.rideaustin.model.enums.ActiveDriverStatus;
import com.rideaustin.model.enums.ApiClientAppType;
import com.rideaustin.model.ride.ActiveDriver;
import com.rideaustin.model.user.User;
import com.rideaustin.repo.dsl.UserDslRepository;
import com.rideaustin.rest.model.ErrorMessageDto;
import com.rideaustin.service.ActiveDriversService;
import com.rideaustin.service.CurrentUserService;
import com.rideaustin.service.SessionService;
import com.rideaustin.service.security.BasicAndTokenAuthenticationService;
import com.rideaustin.utils.CryptUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class BasicAndTokenAuthenticationFilter extends OncePerRequestFilter {

  private static final String TOKEN_HEADER_NAME = "X-Auth-Token";

  private final PasswordEncoder passwordEncoder;
  private final UserDslRepository userDslRepository;
  private final CurrentUserService currentUserService;
  private final ActiveDriversService activeDriversService;
  private final SessionService sessionService;
  private final BasicAndTokenAuthenticationService basicAndTokenAuthenticationService;
  private final CryptUtils cryptUtils;

  @Override
  public void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws IOException,
    ServletException {

    log.debug("BasicAndTokenAuthenticationFilter executed");
    if (attemptAuthentication(request)) {
      log.debug("User was correctly authenticated");
    } else {
      log.debug("User was not correctly authenticated");
    }
    filterChain.doFilter(request, response);
  }

  private boolean attemptAuthentication(ServletRequest request) {

    HttpServletRequest httpServletRequest = (HttpServletRequest) request;
    String token = httpServletRequest.getHeader(TOKEN_HEADER_NAME);

    ClientAppVersion clientAppVersion = new ClientAppVersionFactory().createClientAppVersion(httpServletRequest);
    String userAgent = "";
    String userDeviceId = "";
    String login = null;
    String password = null;

    String authorizationParam = httpServletRequest.getHeader(HttpHeaders.AUTHORIZATION);
    if (authorizationParam != null && !authorizationParam.isEmpty()) {
      String[] decodedCredentials = BasicAndTokenAuthenticationService.decodeBasicAuthKey(authorizationParam);
      if (decodedCredentials.length != 2) {
        return true;
      }
      login = decodedCredentials[0];
      password = decodedCredentials[1];
    }

    if (clientAppVersion != null) {
      userAgent = clientAppVersion.getUserAgent();
      userDeviceId = clientAppVersion.getUserDeviceId();
    }
    ApiClientAppType apiClientAppType = ApiClientAppType.getSuggestedAvatarTypeFromHeader(userAgent);

    log.debug("Loaded token: {}", token);
    log.debug("Loaded login: {}", login);
    log.debug("Loaded password: {}", password);
    log.debug("Loaded authorizationParam: {}", authorizationParam);
    log.debug("Loaded user agent: {}", userAgent);
    log.debug("Parsed api client type: {}", apiClientAppType);
    log.debug("User device id: {}", userDeviceId);

    boolean useTokenAuth = StringUtils.isNotBlank(token);
    boolean useBasicAuth = (login != null) && (password != null);

    if (useBasicAuth) {
      log.debug("Authenticate using basic auth params {}", login);
      return authenticateByLoginAndPassword(login, password, apiClientAppType, userDeviceId, httpServletRequest);
    } else if (useTokenAuth) {
      log.debug("Authenticate based on token {}", token);
      return authenticateByToken(token, apiClientAppType, httpServletRequest, userAgent);
    }
    basicAndTokenAuthenticationService.storeReason("Authentication credentials were not provided", ErrorMessageDto.ReasonKey.CREDENTIALS_NOT_PROVIDED, httpServletRequest);
    return false;
  }

  private boolean authenticateByLoginAndPassword(String login, String password, ApiClientAppType apiClientAppType, String userDeviceId, HttpServletRequest request) {
    User user = userDslRepository.findByEmail(login);
    if (user == null) {
      log.debug("Cannot find user by username: {}", login);
      basicAndTokenAuthenticationService.storeReason("Invalid username or password ", ErrorMessageDto.ReasonKey.WRONG_CREDENTIALS, request);
      return false;
    }
    log.debug("User for provided login loaded: {}", user.getUsername());

    if (passwordEncoder.matches(password, user.getPassword())
      || passwordEncoder.matches(cryptUtils.clientAppHash(login, password), user.getPassword())) {
      log.debug("Password matches! Continue authentication.");
      log.debug("Adding authentication into spring  security context");

      if (!user.getUserEnabled()) {
        log.debug("User is disabled");
        basicAndTokenAuthenticationService.storeReason("Account is disabled. Please contact support.", ErrorMessageDto.ReasonKey.USER_DISABLED, request);
        return false;
      }
      if (apiClientAppType.equals(ApiClientAppType.MOBILE_DRIVER) && !user.isDriver()) {
        log.debug("Trying to log-in as rider from driver app");
        basicAndTokenAuthenticationService.storeReason("Invalid driver username or password", ErrorMessageDto.ReasonKey.WRONG_CREDENTIALS, request);
        return false;
      }

      Session session = sessionService.getSessionByLoginWithFallback(login, apiClientAppType);
      if (session != null && session.getUser() != null) {
        log.debug("There is already pending session for that user and client type: {}", apiClientAppType);
        if (session.getUserDeviceId() != null && session.getUserDeviceId().equals(userDeviceId)) {
          log.debug("Trying to login another time on same apiClient and device");
        } else {
          log.debug("User logged in using other device");
          ActiveDriver ad = activeDriversService.getActiveDriverByDriver(user);
          if (ad != null && ApiClientAppType.MOBILE_DRIVER.equals(apiClientAppType) && ActiveDriverStatus.RIDING.equals(ad.getStatus())) {
            basicAndTokenAuthenticationService.handleLoggedOnOtherDevice(request, session);
            return false;
          }
        }
      }
      currentUserService.setUser(user);
      return true;
    } else {
      log.debug("Password does not match");
      basicAndTokenAuthenticationService.storeReason("Invalid username or password ", ErrorMessageDto.ReasonKey.WRONG_CREDENTIALS, request);
      return false;
    }
  }

  private boolean authenticateByToken(String token, ApiClientAppType apiClientAppType, HttpServletRequest request, String userAgent) {
    Optional<Session> sessionOptional = sessionService.retrieveSession(token, apiClientAppType);
    log.debug("Token based session loaded: {}", sessionOptional);
    final boolean isTokenValid = sessionOptional.isPresent() && sessionOptional.map(Session::getUser).isPresent();
    if (isTokenValid) {
      Session session = sessionOptional.get();
      session = basicAndTokenAuthenticationService.handleUserAgentChange(session, apiClientAppType, userAgent);
      log.debug("The provided session token is valid: {}. User found: {}", session.getAuthToken(), session.getUser().getEmail());
      log.debug("Adding authentication into spring  security context");
      currentUserService.setUser(session.getUser());
      return true;
    }
    log.debug("Token was invalid. Preparing reason message");
    basicAndTokenAuthenticationService.handleTokenInvalidReason(token, apiClientAppType, request);
    return false;
  }

}