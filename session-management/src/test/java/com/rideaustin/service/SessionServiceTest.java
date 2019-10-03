package com.rideaustin.service;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;

import com.rideaustin.model.Session;
import com.rideaustin.model.enums.ApiClientAppType;
import com.rideaustin.model.enums.AvatarType;
import com.rideaustin.model.enums.SessionClosingReason;
import com.rideaustin.model.user.User;
import com.rideaustin.repo.dsl.BlockedDeviceDslRepository;
import com.rideaustin.repo.dsl.SessionDslRepository;
import com.rideaustin.repo.jpa.SessionRepository;
import com.rideaustin.service.user.BlockedDeviceRegistry;

@RunWith(MockitoJUnitRunner.class)
public class SessionServiceTest {

  private static final String TOKEN = "123:123123123123123";
  private static final long ID = 123L;
  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Mock
  private SessionRepository sessionRepository;
  @Mock
  private SessionDslRepository sessionDslRepository;
  @Mock
  private SessionCache sessionCache;
  @Mock
  private RedisTemplate redisTemplate;
  @Mock
  private ChannelTopic channelTopic;
  @Mock
  private TokenService tokenService;
  @Mock
  private BlockedDeviceDslRepository blockedDeviceDslRepository;
  @Mock
  private BlockedDeviceRegistry blockedDeviceRegistry;

  private SessionService sessionService;

  @Before
  public void setup() throws Exception {
    sessionService = new SessionService(sessionRepository, sessionCache, redisTemplate, channelTopic,
      sessionDslRepository, blockedDeviceDslRepository, blockedDeviceRegistry, tokenService);
  }

  @Test
  public void retrieveSessionEmptyToken() {
    Optional<Session> result = sessionService.retrieveSession("", ApiClientAppType.MOBILE_DRIVER);
    assertThat(result.isPresent(), is(false));
  }

  @Test
  public void retrieveSessionNullToken() {
    Optional<Session> result = sessionService.retrieveSession(null, ApiClientAppType.MOBILE_DRIVER);
    assertThat(result.isPresent(), is(false));
  }

  @Test
  public void retrieveSessionTokenFromDistributedCache() {
    when(sessionCache.retrieve("123", ApiClientAppType.MOBILE_DRIVER)).thenReturn(mockSession(TOKEN));
    Optional<Session> result = sessionService.retrieveSession(TOKEN, ApiClientAppType.MOBILE_DRIVER);

    verify(sessionDslRepository, never()).findByAuthTokenAndClientWithUser(TOKEN, ApiClientAppType.MOBILE_DRIVER);
    assertThat(result.isPresent(), is(true));
  }

  @Test
  public void retrieveSessionTokenNoSession() {

    when(sessionCache.retrieve("123", ApiClientAppType.MOBILE_DRIVER)).thenReturn(null);
    Optional<Session> result = sessionService.retrieveSession(TOKEN, ApiClientAppType.MOBILE_DRIVER);

    verify(sessionCache, times(1)).retrieve("123", ApiClientAppType.MOBILE_DRIVER);
    verify(sessionDslRepository, times(1)).findByAuthTokenAndClientWithUser(TOKEN, ApiClientAppType.MOBILE_DRIVER);
    assertThat(result.isPresent(), is(false));
  }

  @Test
  public void retrieveSessionTokenSessionFromDB() {

    Session s = mockSession(TOKEN);

    when(sessionCache.retrieve("123", ApiClientAppType.MOBILE_DRIVER)).thenReturn(null);
    when(sessionDslRepository.findByAuthTokenAndClientWithUser(TOKEN, ApiClientAppType.MOBILE_DRIVER)).thenReturn(s);
    Optional<Session> result = sessionService.retrieveSession(TOKEN, ApiClientAppType.MOBILE_DRIVER);

    verify(sessionCache, times(1)).retrieve("123", ApiClientAppType.MOBILE_DRIVER);
    verify(sessionDslRepository, times(1)).findByAuthTokenAndClientWithUser(TOKEN, ApiClientAppType.MOBILE_DRIVER);
    verify(sessionCache, times(1)).store(any(Session.class), any());
    assertThat(result.isPresent(), is(true));
  }

  @Test
  public void testRetrieveSessionNonMobileClient() {
    when(sessionCache.retrieve(anyString(), any())).thenReturn(new Session());

    Optional<Session> result = sessionService.retrieveSession(TOKEN, ApiClientAppType.OTHER);

    verify(sessionDslRepository, never()).findByAuthTokenAndClientWithUser(TOKEN, ApiClientAppType.OTHER);
    assertThat(result.isPresent(), is(true));
  }

  @Test
  public void testEndSessionWithRider(){
    User user = mockUser(1L);
    ApiClientAppType appType = ApiClientAppType.MOBILE_RIDER;
    SessionClosingReason reason = SessionClosingReason.LOGOUT;

    sessionService.endSession(user, appType, reason);

    verify(tokenService, times(1)).deleteTokensAndTopicSubscriptions(eq(user), eq(AvatarType.RIDER));
  }

  @Test
  public void testEndSessionWithDriver(){
    User user = mockUser(1L);
    ApiClientAppType appType = ApiClientAppType.MOBILE_DRIVER;
    SessionClosingReason reason = SessionClosingReason.LOGOUT;

    sessionService.endSession(user, appType, reason);

    verify(tokenService, times(1)).deleteTokensAndTopicSubscriptions(eq(user), eq(AvatarType.DRIVER));
  }

  private User mockUser(long id) {
    User u = new User();
    u.setId(id);
    return u;
  }

  private Session mockSession(String token) {
    Session s = new Session();

    s.setUser(mockUser(ID));
    s.setAuthToken(token);
    s.setApiClientAppType(ApiClientAppType.MOBILE_DRIVER);
    return s;
  }

}
