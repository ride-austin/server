package com.rideaustin.service;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Date;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.common.collect.ImmutableList;
import com.rideaustin.model.user.Rider;
import com.rideaustin.model.user.User;
import com.rideaustin.repo.jpa.SessionRepository;

@RunWith(MockitoJUnitRunner.class)
public class BaseAvatarServiceTest {

  private static final Date LAST_LOGIN_DATE = new Date();
  private static final Date LAST_LOGIN_DATE2 = new Date(1488431844);
  private static final long USER_ID = 1L;
  private static final long USER_ID2 = 2L;

  @Mock
  private SessionRepository sessionRepository;

  @InjectMocks
  private BaseAvatarService baseAvatarService;

  @Test
  public void shouldSetLastLoginDateForOneRider() {
    // given
    Rider rider = getRider(USER_ID);
    when(sessionRepository.findLastLoginDateByUser(rider.getUser())).thenReturn(LAST_LOGIN_DATE);

    // when
    baseAvatarService.enrichAvatarWithLastLoginDate(rider);

    // then
    verify(sessionRepository, times(1)).findLastLoginDateByUser(rider.getUser());
    assertEquals(LAST_LOGIN_DATE, rider.getLastLoginDate());
  }

  @Test
  public void shouldSetLastLoginDateForTwoRiders() {
    // given
    Rider rider1 = getRider(USER_ID);
    Rider rider2 = getRider(USER_ID2);
    when(sessionRepository.findLastLoginDateByUsers(asList(rider1.getUser(), rider2.getUser()))).thenReturn(ImmutableList.of(
      ImmutablePair.of(USER_ID, LAST_LOGIN_DATE),
      ImmutablePair.of(USER_ID2, LAST_LOGIN_DATE2)
    ));

    // when
    baseAvatarService.enrichAvatarWithLastLoginDate(asList(rider1, rider2));

    // then
    verify(sessionRepository, times(1)).findLastLoginDateByUsers(asList(rider1.getUser(), rider2.getUser()));
    assertEquals(LAST_LOGIN_DATE, rider1.getLastLoginDate());
    assertEquals(LAST_LOGIN_DATE2, rider2.getLastLoginDate());
  }

  private Rider getRider(long userId) {
    Rider rider = new Rider();
    User user = new User();
    user.setId(userId);
    rider.setUser(user);
    return rider;
  }
}