package com.rideaustin.service.security;

import static com.rideaustin.service.security.BasicAndTokenAuthenticationService.AUTHENTICATION_ERROR_MESSAGE;
import static com.rideaustin.service.security.BasicAndTokenAuthenticationService.LOGGED_IN_OTHER_DEVICE_FORMAT_MESSAGE;
import static com.rideaustin.service.security.BasicAndTokenAuthenticationService.LOGGED_IN_OTHER_DEVICE_MESSAGE;
import static com.rideaustin.service.security.BasicAndTokenAuthenticationService.LOGGED_IN_OTHER_DEVICE_NO_FORMAT_MESSAGE;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.EnumSet;

import javax.servlet.http.HttpServletRequest;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.rideaustin.model.Session;
import com.rideaustin.model.enums.ApiClientAppType;
import com.rideaustin.model.enums.SessionClosingReason;
import com.rideaustin.model.user.User;
import com.rideaustin.rest.model.ErrorMessageDto;
import com.rideaustin.service.SessionService;
import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;

@RunWith(DataProviderRunner.class)
public class BasicAndTokenAuthenticationServiceTest {

  private static final String TOKEN = "token";

  @Mock
  private SessionService sessionService;
  @Mock
  private HttpServletRequest request;

  private Session session;

  private BasicAndTokenAuthenticationService testedInstance;

  @DataProvider
  public static Object[] invalidClosingReasons() {
    return EnumSet.complementOf(EnumSet.of(SessionClosingReason.LOGOUT, SessionClosingReason.SESSION_EXPIRE)).toArray();
  }

  @DataProvider
  public static Object[] mobileDeviceTypes() {
    return EnumSet.of(ApiClientAppType.MOBILE_DRIVER, ApiClientAppType.MOBILE_RIDER).toArray();
  }

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    testedInstance = new BasicAndTokenAuthenticationService(sessionService);

    session = new Session();
    User user = new User();
    user.setEmail("a@b.c");
    session.setUser(user);
  }

  @Test
  public void testHandleTokenInvalidReasonStoresReasonInvalidSessionToken() {
    when(sessionService.findDeletedByAuthTokenWithUser(any())).thenReturn(null);

    testedInstance.handleTokenInvalidReason("", null, request);

    assertReasonStored(AUTHENTICATION_ERROR_MESSAGE, ErrorMessageDto.ReasonKey.INVALID_SESSION_TOKEN);
  }

  @Test
  public void testHandleTokenInvalidReasonStoresReasonLoggedOut() {
    session.setSessionClosingReason(SessionClosingReason.LOGOUT);
    when(sessionService.findDeletedByAuthTokenWithUser(any())).thenReturn(session);
    when(sessionService.findCurrentSessionByUserEmailAndAppTypeWithUser(anyString(), any())).thenReturn(null);

    testedInstance.handleTokenInvalidReason("", null, request);

    assertReasonStored(AUTHENTICATION_ERROR_MESSAGE, ErrorMessageDto.ReasonKey.LOGGED_OUT);
  }

  @Test
  public void testHandleTokenInvalidReasonStoresReasonSessionExpired() {
    session.setSessionClosingReason(SessionClosingReason.SESSION_EXPIRE);
    when(sessionService.findDeletedByAuthTokenWithUser(any())).thenReturn(session);
    when(sessionService.findCurrentSessionByUserEmailAndAppTypeWithUser(anyString(), any())).thenReturn(null);

    testedInstance.handleTokenInvalidReason("", null, request);

    assertReasonStored(AUTHENTICATION_ERROR_MESSAGE, ErrorMessageDto.ReasonKey.SESSION_EXPIRED);
  }

  @Test
  @UseDataProvider("invalidClosingReasons")
  public void testHandleTokenInvalidReasonStoresReasonInvalidSessionTokenOnInvalidClosingReasons(SessionClosingReason reason) {
    session.setSessionClosingReason(reason);
    when(sessionService.findDeletedByAuthTokenWithUser(any())).thenReturn(session);
    when(sessionService.findCurrentSessionByUserEmailAndAppTypeWithUser(anyString(), any())).thenReturn(null);

    testedInstance.handleTokenInvalidReason("", null, request);

    assertReasonStored(AUTHENTICATION_ERROR_MESSAGE, ErrorMessageDto.ReasonKey.INVALID_SESSION_TOKEN);
  }

  @Test
  @UseDataProvider("mobileDeviceTypes")
  public void testHandleTokenInvalidReasonStoresReasonDuplicateDeviceOnMobileDeviceTypes(ApiClientAppType appType) {
    when(sessionService.findDeletedByAuthTokenWithUser(any())).thenReturn(session);
    when(sessionService.findCurrentSessionByUserEmailAndAppTypeWithUser(anyString(), any())).thenReturn(session);

    testedInstance.handleTokenInvalidReason("", appType, request);

    assertReasonStored(LOGGED_IN_OTHER_DEVICE_MESSAGE, ErrorMessageDto.ReasonKey.LOGGED_IN_ON_OTHER_DEVICE);
  }

  @Test
  public void testHandleTokenInvalidReasonStoresReasonDuplicateDeviceOnConsole() {
    when(sessionService.findDeletedByAuthTokenWithUser(any())).thenReturn(session);
    when(sessionService.findCurrentSessionByUserEmailAndAppTypeWithUser(anyString(), any())).thenReturn(session);

    testedInstance.handleTokenInvalidReason("", ApiClientAppType.OTHER, request);

    verify(request, never()).setAttribute(eq(ErrorMessageDto.REASON), any());
    verify(request, never()).setAttribute(eq(ErrorMessageDto.REASON_KEY), any());
  }

  @Test
  public void testHandleLoggedOnOtherDeviceWithoutUserDevice() {
    session.setUserDevice("device");

    testedInstance.handleLoggedOnOtherDevice(request, session);

    assertReasonStored(String.format(LOGGED_IN_OTHER_DEVICE_FORMAT_MESSAGE, session.getUserDevice()), ErrorMessageDto.ReasonKey.LOGGED_IN_ON_OTHER_DEVICE);
  }

  @Test
  public void testHandleLoggedOnOtherDeviceWithUserDevice() {
    testedInstance.handleLoggedOnOtherDevice(request, session);

    assertReasonStored(LOGGED_IN_OTHER_DEVICE_NO_FORMAT_MESSAGE, ErrorMessageDto.ReasonKey.LOGGED_IN_ON_OTHER_DEVICE);
  }

  @Test
  @UseDataProvider("mobileDeviceTypes")
  public void testHandleUserAgentChangeReplacesSession(ApiClientAppType appType) {
    String userAgent = "UA";
    String otherUserAgent = "Other-UA";

    session.setApiClientAppType(appType);
    session.setUserAgent(userAgent);
    session.setAuthToken(TOKEN);

    testedInstance.handleUserAgentChange(session, appType, otherUserAgent);

    verify(sessionService, times(1)).replaceSession(session, otherUserAgent, appType, TOKEN);
  }

  @Test
  public void testHandleUserAgentChangeSkipsSessionReplaceOnConsole() {
    String userAgent = "UA";
    String otherUserAgent = "Other-UA";

    session.setApiClientAppType(ApiClientAppType.OTHER);
    session.setUserAgent(userAgent);

    testedInstance.handleUserAgentChange(session, ApiClientAppType.OTHER, otherUserAgent);

    verify(sessionService, never()).replaceSession(session, otherUserAgent, ApiClientAppType.OTHER, TOKEN);
  }

  @Test
  public void testHandleUserAgentChangeSkipsSessionReplaceOnDifferentAppType() {
    String userAgent = "UA";
    String otherUserAgent = "Other-UA";

    session.setApiClientAppType(ApiClientAppType.MOBILE_RIDER);
    session.setUserAgent(userAgent);

    testedInstance.handleUserAgentChange(session, ApiClientAppType.MOBILE_DRIVER, otherUserAgent);

    verify(sessionService, never()).replaceSession(session, otherUserAgent, ApiClientAppType.MOBILE_DRIVER, TOKEN);
  }

  @Test
  public void testHandleUserAgentChangeSkipsSessionReplaceOnSameUserAgent() {
    String userAgent = "UA";

    session.setApiClientAppType(ApiClientAppType.MOBILE_RIDER);
    session.setUserAgent(userAgent);

    testedInstance.handleUserAgentChange(session, ApiClientAppType.MOBILE_RIDER, userAgent);

    verify(sessionService, never()).replaceSession(session, userAgent, ApiClientAppType.MOBILE_DRIVER, TOKEN);
  }

  private void assertReasonStored(String reason, ErrorMessageDto.ReasonKey reasonKey) {
    verify(request, times(1)).setAttribute(ErrorMessageDto.REASON, reason);
    verify(request, times(1)).setAttribute(ErrorMessageDto.REASON_KEY, reasonKey);
  }

}