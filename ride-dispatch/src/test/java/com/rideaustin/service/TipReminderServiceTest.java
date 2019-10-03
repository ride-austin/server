package com.rideaustin.service;

import static org.junit.Assert.*;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.rideaustin.model.ride.Ride;
import com.rideaustin.model.user.User;
import com.rideaustin.repo.dsl.RideDslRepository;
import com.rideaustin.service.notifications.PushNotificationsFacade;

public class TipReminderServiceTest {

  @Mock
  private RideDslRepository rideDslRepository;
  @Mock
  private PushNotificationsFacade pushNotificationsFacade;

  private TipReminderService testedInstance;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);

    testedInstance = new TipReminderService(rideDslRepository, pushNotificationsFacade);
  }

  @Test
  public void sendTipReminderToRiderSendsPushNotification() {
    final Ride ride = new Ride();
    final User user = new User();
    when(rideDslRepository.findOne(anyLong())).thenReturn(ride);
    when(rideDslRepository.findRiderUser(anyLong())).thenReturn(user);

    testedInstance.sendTipReminderToRider(1L);

    verify(pushNotificationsFacade).pushTipReminderNotificationToRider(ride, user);
  }
}