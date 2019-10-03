package com.rideaustin.security;

import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.stubbing.Answer;
import org.springframework.http.HttpHeaders;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.rideaustin.model.Session;
import com.rideaustin.model.enums.ActiveDriverStatus;
import com.rideaustin.model.enums.ApiClientAppType;
import com.rideaustin.model.ride.ActiveDriver;
import com.rideaustin.model.user.Driver;
import com.rideaustin.model.user.Rider;
import com.rideaustin.model.user.User;
import com.rideaustin.repo.dsl.UserDslRepository;
import com.rideaustin.rest.model.ErrorMessageDto;
import com.rideaustin.service.ActiveDriversService;
import com.rideaustin.service.CurrentUserService;
import com.rideaustin.service.SessionService;
import com.rideaustin.service.security.BasicAndTokenAuthenticationService;
import com.rideaustin.utils.CryptUtils;

public class BasicAndTokenAuthenticationFilterTest {

  @Mock
  private PasswordEncoder passwordEncoder;
  @Mock
  private UserDslRepository userDslRepository;
  @Mock
  private CurrentUserService currentUserService;
  @Mock
  private ActiveDriversService activeDriversService;
  @Mock
  private SessionService sessionService;
  @Mock
  private BasicAndTokenAuthenticationService basicAndTokenAuthenticationService;
  @Mock
  private CryptUtils cryptUtils;
  @Mock
  private HttpServletRequest request;
  @Mock
  private HttpServletResponse response;
  @Mock
  private FilterChain filterChain;

  private BasicAndTokenAuthenticationFilter testedInstance;
  public static final String PASSWORD = "test123";
  public static final String EMAIL = "user@example.com";

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);

    testedInstance = new BasicAndTokenAuthenticationFilter(passwordEncoder, userDslRepository, currentUserService, activeDriversService,
      sessionService, basicAndTokenAuthenticationService, cryptUtils);
  }

  @Test
  public void filterSets401OnNoCredentials() throws IOException, ServletException {
    testedInstance.doFilterInternal(request, response, filterChain);

    verify(basicAndTokenAuthenticationService, times(1))
      .storeReason(eq("Authentication credentials were not provided"), eq(ErrorMessageDto.ReasonKey.CREDENTIALS_NOT_PROVIDED), eq(request));
  }

  @Test
  public void filterSets401OnMissingUsername() throws IOException, ServletException {
    setupBasicAuthorization();
    testedInstance.doFilterInternal(request, response, filterChain);

    verify(basicAndTokenAuthenticationService, times(1))
      .storeReason(eq("Invalid username or password "), eq(ErrorMessageDto.ReasonKey.WRONG_CREDENTIALS), eq(request));
  }

  @Test
  public void filterSets401OnWrongPassword() throws IOException, ServletException {
    setupBasicAuthorization();
    final User user = new User();
    user.setEmail(EMAIL);
    when(userDslRepository.findByEmail(EMAIL)).thenReturn(user);
    when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

    testedInstance.doFilterInternal(request, response, filterChain);

    verify(basicAndTokenAuthenticationService, times(1))
      .storeReason(eq("Invalid username or password "), eq(ErrorMessageDto.ReasonKey.WRONG_CREDENTIALS), eq(request));
  }

  @Test
  public void filterSets401OnDisabledUser() throws IOException, ServletException {
    setupBasicAuthorization();
    final User user = new User();
    user.setEmail(EMAIL);
    user.setUserEnabled(false);
    when(userDslRepository.findByEmail(EMAIL)).thenReturn(user);
    when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);

    testedInstance.doFilterInternal(request, response, filterChain);

    verify(basicAndTokenAuthenticationService, times(1))
      .storeReason(eq("Account is disabled. Please contact support."), eq(ErrorMessageDto.ReasonKey.USER_DISABLED), eq(request));
  }

  @Test
  public void filterSets401OnWrongApp() throws IOException, ServletException {
    setupBasicAuthorization();
    when(request.getHeader(eq(HttpHeaders.USER_AGENT))).thenReturn("RideAustinDriver");
    final User user = new User();
    user.setEmail(EMAIL);
    user.setUserEnabled(true);
    when(userDslRepository.findByEmail(EMAIL)).thenReturn(user);
    when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);

    testedInstance.doFilterInternal(request, response, filterChain);

    verify(basicAndTokenAuthenticationService, times(1))
      .storeReason(eq("Invalid driver username or password"), eq(ErrorMessageDto.ReasonKey.WRONG_CREDENTIALS), eq(request));
  }

  @Test
  public void filterSets401ForDriverLoggingInWhileInARide() throws IOException, ServletException {
    setupBasicAuthorization();
    when(request.getHeader(eq(HttpHeaders.USER_AGENT))).thenReturn("RideAustinDriver");
    final User user = new User();
    user.setEmail(EMAIL);
    user.setUserEnabled(true);
    user.addAvatar(new Driver());
    final Session session = new Session();
    session.setUser(user);
    session.setUserDeviceId(UUID.randomUUID().toString());
    final ActiveDriver activeDriver = new ActiveDriver();
    activeDriver.setStatus(ActiveDriverStatus.RIDING);
    when(userDslRepository.findByEmail(EMAIL)).thenReturn(user);
    when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
    when(sessionService.getSessionByLoginWithFallback(eq(EMAIL), eq(ApiClientAppType.MOBILE_DRIVER)))
      .thenReturn(session);
    when(activeDriversService.getActiveDriverByDriver(eq(user))).thenReturn(activeDriver);

    testedInstance.doFilterInternal(request, response, filterChain);

    verify(basicAndTokenAuthenticationService).handleLoggedOnOtherDevice(eq(request), eq(session));
  }

  @Test
  public void filterSetsUserForRider() throws IOException, ServletException {
    setupBasicAuthorization();
    when(request.getHeader(eq(HttpHeaders.USER_AGENT))).thenReturn("RideAustin");
    final User user = new User();
    user.setEmail(EMAIL);
    user.setUserEnabled(true);
    user.addAvatar(new Rider());

    when(userDslRepository.findByEmail(EMAIL)).thenReturn(user);
    when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);

    testedInstance.doFilterInternal(request, response, filterChain);

    verify(currentUserService, only()).setUser(eq(user));
  }

  @Test
  public void filterSetsUserForDriver() throws IOException, ServletException {
    setupBasicAuthorization();
    when(request.getHeader(eq(HttpHeaders.USER_AGENT))).thenReturn("RideAustinDriver");
    final User user = new User();
    user.setEmail(EMAIL);
    user.setUserEnabled(true);
    user.addAvatar(new Driver());

    when(userDslRepository.findByEmail(EMAIL)).thenReturn(user);
    when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);

    testedInstance.doFilterInternal(request, response, filterChain);

    verify(currentUserService, only()).setUser(eq(user));
  }

  @Test
  public void filterSets401OnInvalidToken() throws IOException, ServletException {
    final String token = String.format("%s:%s", 123, UUID.randomUUID().toString().toLowerCase());
    when(request.getHeader("X-Auth-Token")).thenReturn(token);
    when(request.getHeader(eq(HttpHeaders.USER_AGENT))).thenReturn("RideAustinDriver");
    when(sessionService.retrieveSession(eq(token), eq(ApiClientAppType.MOBILE_DRIVER))).thenReturn(Optional.empty());

    testedInstance.doFilterInternal(request, response, filterChain);

    verify(basicAndTokenAuthenticationService, times(1)).handleTokenInvalidReason(eq(token),
      eq(ApiClientAppType.MOBILE_DRIVER), eq(request));
  }

  @Test
  public void filterSetsUserForRiderByToken() throws IOException, ServletException {
    final String token = String.format("%s:%s", 123, UUID.randomUUID().toString().toLowerCase());
    final String userAgent = "RideAustin";
    final Session session = new Session();
    final User user = new User();
    final ApiClientAppType clientAppType = ApiClientAppType.MOBILE_RIDER;
    user.setEmail(EMAIL);
    session.setUser(user);
    session.setAuthToken(token);

    when(request.getHeader("X-Auth-Token")).thenReturn(token);
    when(request.getHeader(eq(HttpHeaders.USER_AGENT))).thenReturn(userAgent);

    when(sessionService.retrieveSession(eq(token), eq(clientAppType))).thenReturn(Optional.of(session));
    when(basicAndTokenAuthenticationService.handleUserAgentChange(eq(session), eq(clientAppType), eq(userAgent)))
      .thenAnswer((Answer<Session>) invocation -> (Session) invocation.getArguments()[0]);

    testedInstance.doFilterInternal(request, response, filterChain);

    verify(basicAndTokenAuthenticationService, times(1)).handleUserAgentChange(eq(session), eq(clientAppType), eq(userAgent));
    verify(currentUserService, only()).setUser(eq(user));
  }

  @Test
  public void filterSetsUserForDriverByToken() throws IOException, ServletException {
    final String token = String.format("%s:%s", 123, UUID.randomUUID().toString().toLowerCase());
    final String userAgent = "RideAustinDriver";
    final Session session = new Session();
    final User user = new User();
    final ApiClientAppType clientAppType = ApiClientAppType.MOBILE_DRIVER;
    user.setEmail(EMAIL);
    session.setUser(user);
    session.setAuthToken(token);

    when(request.getHeader("X-Auth-Token")).thenReturn(token);
    when(request.getHeader(eq(HttpHeaders.USER_AGENT))).thenReturn(userAgent);

    when(sessionService.retrieveSession(eq(token), eq(clientAppType))).thenReturn(Optional.of(session));
    when(basicAndTokenAuthenticationService.handleUserAgentChange(eq(session), eq(clientAppType), eq(userAgent)))
      .thenAnswer((Answer<Session>) invocation -> (Session) invocation.getArguments()[0]);

    testedInstance.doFilterInternal(request, response, filterChain);

    verify(basicAndTokenAuthenticationService, times(1))
      .handleUserAgentChange(eq(session), eq(clientAppType), eq(userAgent));
    verify(currentUserService, only()).setUser(eq(user));
  }

  private void setupBasicAuthorization() {
    when(request.getHeader(eq(HttpHeaders.AUTHORIZATION)))
      .thenReturn(String.format("Basic %s", Base64.getEncoder().encodeToString(String.format("%s:%s", EMAIL, PASSWORD).getBytes())));
  }
}