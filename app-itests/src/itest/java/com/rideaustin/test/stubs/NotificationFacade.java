package com.rideaustin.test.stubs;

import java.util.HashMap;
import java.util.Map;

import org.springframework.core.env.Environment;

import com.rideaustin.model.RidePushNotificationRepository;
import com.rideaustin.model.Session;
import com.rideaustin.model.enums.AvatarType;
import com.rideaustin.model.enums.PaymentStatus;
import com.rideaustin.model.ride.Ride;
import com.rideaustin.model.user.User;
import com.rideaustin.repo.jpa.TokenRepository;
import com.rideaustin.service.CityCache;
import com.rideaustin.service.notification.RideFlowPushNotificationFacade;
import com.rideaustin.service.notifications.PushNotificationsService;

public class NotificationFacade extends RideFlowPushNotificationFacade {

  private User user;
  private Map<String, String> dataMap;

  public NotificationFacade(TokenRepository tokenRepository, PushNotificationsService notificationsService, CityCache cityCache, Environment environment, RidePushNotificationRepository repository) {
    super(tokenRepository, cityCache, notificationsService, environment, repository);
    dataMap = new HashMap<>();
  }

  @Override
  protected void pushNotificationToRider(User user, Map<String, String> dataMap) {
    this.dataMap = dataMap;
    this.user = user;
  }

  @Override
  public void pushRiderPaymentUpdate(User user, PaymentStatus paymentStatus, Session session) {

  }

  @Override
  public void pushCallBlockedNotification(Ride ride, String callerNumber, User targetUser) {

  }

  @Override
  public void pushSplitFareAcceptanceToRider(Long rideId, Long splitFareId, User sourceUser, User targetUser, boolean acceptanceStatus) {

  }

  @Override
  public void pushSplitFareRequestToRider(Long rideId, Long splitFareId, User sourceUser, User targetUser) {

  }

  @Override
  public void pushTextNotification(User user, AvatarType avatarType, String message) {

  }

  @Override
  public void pushTipReminderNotificationToRider(Ride ride, User targetUser) {

  }

  @Override
  public void pushRideRedispatchNotification(long rideId) {

  }

  public User getUser() {
    return user;
  }

  public Map<String, String> getDataMap() {
    return dataMap;
  }
}
