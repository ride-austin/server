package com.rideaustin.service;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.google.common.collect.Lists;
import com.rideaustin.model.Session;
import com.rideaustin.model.enums.ApiClientAppType;
import com.rideaustin.model.user.User;
import com.rideaustin.repo.jpa.SessionRepository;

public class CurrentSessionServiceTest {

  private static final long ID = 123L;
  private static final String TOKEN = "123:123123123123123";

  @Mock
  private SessionRepository sessionRepository;

  @Mock
  private SessionCache sessionCache;

  private CurrentSessionService testedInstance;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
    testedInstance = new CurrentSessionService(sessionRepository, sessionCache);
  }

  @Test
  public void testRefreshUserSessionOneSession() {
    Session s = mockSession(TOKEN);
    when(sessionRepository.findActiveSessionsByUser(ID)).thenReturn(Lists.newArrayList(s));
    testedInstance.refreshUserSession(mockUser(ID), ApiClientAppType.MOBILE_DRIVER);
    verify(sessionCache, times(1)).store(any(Session.class), any());
    verify(sessionRepository, never()).saveAndFlush(s);
  }

  @Test
  public void testRefreshUserSessionTwoSessions() {
    Session s1 = mockSession(TOKEN);
    Session s2 = mockSession(TOKEN);
    when(sessionRepository.findActiveSessionsByUser(ID)).thenReturn(Lists.newArrayList(s1, s2));
    testedInstance.refreshUserSession(mockUser(ID), ApiClientAppType.MOBILE_DRIVER);
    verify(sessionCache, times(1)).store(any(Session.class), any());
    verify(sessionRepository, times(1)).findActiveSessionsByUser(ID);
    verify(sessionRepository, times(1)).saveAndFlush(s2);
  }

  private User mockUser(long id) {
    User u = new User();
    u.setId(id);
    return u;
  }

  private Session mockSession(String token) {
    Session s = new Session();
    s.setUser(mockUser(123L));
    s.setAuthToken(token);
    s.setApiClientAppType(ApiClientAppType.MOBILE_DRIVER);
    return s;
  }
}