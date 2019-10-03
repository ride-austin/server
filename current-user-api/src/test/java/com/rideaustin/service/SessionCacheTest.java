package com.rideaustin.service;

import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.verify;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.rideaustin.model.Session;
import com.rideaustin.model.enums.ApiClientAppType;
import com.rideaustin.model.user.User;
import com.rideaustin.repo.redis.SessionRedisRepository;

public class SessionCacheTest {

  @Mock
  private SessionRedisRepository sessionRedisRepository;

  private RedisKeySessionMatcher sessionMatcher = new RedisKeySessionMatcher();
  private RedisKeyIdMatcher idMatcher = new RedisKeyIdMatcher();

  private SessionCache testedInstance;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);

    testedInstance = new SessionCache(sessionRedisRepository);
  }

  @Test
  public void storeSavesSessionInRedis() {
    Session session = setupSession();

    testedInstance.store(session, ApiClientAppType.MOBILE_RIDER);

    verify(sessionRedisRepository).save(argThat(sessionMatcher));
  }

  @Test
  public void removeDeletesSessionFromRedis() {
    final Session session = setupSession();
    session.setApiClientAppType(ApiClientAppType.MOBILE_RIDER);

    testedInstance.remove(session);

    verify(sessionRedisRepository).delete(argThat(idMatcher));
  }

  @Test
  public void retrieveFindsSessionInRedis() {
    final Session session = setupSession();

    testedInstance.retrieve(String.valueOf(session.getUser().getId()), ApiClientAppType.MOBILE_RIDER);

    verify(sessionRedisRepository).findOne(argThat(idMatcher));
  }

  private Session setupSession() {
    final long userId = 1L;
    Session session = new Session();
    final User user = new User();
    user.setId(userId);
    session.setUser(user);
    return session;
  }

  private static class RedisKeySessionMatcher extends BaseMatcher<Session> {
    @Override
    public boolean matches(Object o) {
      final Session session = (Session) o;
      return "1:MOBILE_RIDER".equals(session.getRedisKey());
    }

    @Override
    public void describeTo(Description description) {

    }
  }

  private static class RedisKeyIdMatcher extends BaseMatcher<String> {
    @Override
    public boolean matches(Object o) {
      final String id = (String) o;
      return "1:MOBILE_RIDER".equals(id);
    }

    @Override
    public void describeTo(Description description) {

    }
  }
}