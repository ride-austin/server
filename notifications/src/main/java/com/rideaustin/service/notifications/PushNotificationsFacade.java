package com.rideaustin.service.notifications;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.google.common.collect.ImmutableMap;
import com.rideaustin.model.Session;
import com.rideaustin.model.Token;
import com.rideaustin.model.enums.AvatarType;
import com.rideaustin.model.enums.PaymentStatus;
import com.rideaustin.model.ride.Ride;
import com.rideaustin.model.user.User;
import com.rideaustin.repo.jpa.TokenRepository;
import com.rideaustin.rest.exception.ServerError;
import com.rideaustin.service.CityCache;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Primary
@Component
@Profile("!itest")
public class PushNotificationsFacade {

  protected static final String EVENT_KEY = "eventKey";
  protected static final String ALERT = "alert";
  protected static final String SOUND = "sound";
  protected static final String DEFAULT_SOUND = "Update.caf";
  private static final String RIDE_ID = "rideId";

  private final PushNotificationsService notificationsService;
  private final TokenRepository tokenRepository;
  private final CityCache cityCache;
  private final String senderNumber;

  @Inject
  public PushNotificationsFacade(TokenRepository tokenRepository, PushNotificationsService notificationsService,
    CityCache cityCache, Environment environment) {
    this.notificationsService = notificationsService;
    this.tokenRepository = tokenRepository;
    this.cityCache = cityCache;
    this.senderNumber = environment.getProperty("sms.twilio.sender");
  }

  public void pushSplitFareRequestToRider(Long rideId, Long splitFareId, User sourceUser, User targetUser) {
    Map<String, String> dataMap = new HashMap<>();
    dataMap.put(EVENT_KEY, "SPLIT_FARE");
    dataMap.put(ALERT, "SPLIT FARE");
    dataMap.put("sourceUser", sourceUser.getFullName());
    if (sourceUser.getPhotoUrl() != null) {
      dataMap.put("sourceUserPhoto", sourceUser.getPhotoUrl());
    }
    dataMap.put(RIDE_ID, rideId.toString());
    dataMap.put("splitFareId", splitFareId.toString());
    dataMap.put(SOUND, DEFAULT_SOUND);
    pushNotificationToRider(targetUser, dataMap);
  }

  public void pushTipReminderNotificationToRider(Ride ride, User targetUser) {
    Map<String, String> dataMap = new HashMap<>();
    dataMap.put(EVENT_KEY, "RATE_REMINDER");
    dataMap.put("title", "Rate Reminder");
    dataMap.put(RIDE_ID, String.valueOf(ride.getId()));
    dataMap.put(ALERT, String.format("Hope your %s ride was great. Please remember to rate your driver. And remember that optional tips can now be done in-app.", cityCache.getCity(ride.getCityId()).getAppName()));
    dataMap.put(SOUND, DEFAULT_SOUND);
    pushNotificationToRider(targetUser, dataMap);
  }

  public void pushCallBlockedNotification(Ride ride, String callerNumber, User targetUser) {
    Map<String, String> dataMap = new HashMap<>();
    dataMap.put(EVENT_KEY, "CALL_BLOCKED");
    dataMap.put("title", "Call blocked");
    dataMap.put(RIDE_ID, String.valueOf(ride.getId()));
    dataMap.put(ALERT, String.format("%s is trying to reach you but it seems that you added %s to blacklist by sending STOP text message. Please send START text message to %s to get calls from your ride counterpart.", callerNumber, senderNumber, senderNumber));
    dataMap.put(SOUND, DEFAULT_SOUND);
    pushNotificationToRider(targetUser, dataMap);
  }

  public void pushSplitFareAcceptanceToRider(Long rideId, Long splitFareId, User sourceUser, User targetUser, boolean acceptanceStatus) {

    Map<String, String> dataMap = new HashMap<>();
    if (acceptanceStatus) {
      dataMap.put(EVENT_KEY, "SPLIT_FARE_ACCEPTED");
      dataMap.put(ALERT, "SPLIT FARE ACCEPTED");
    } else {
      dataMap.put(EVENT_KEY, "SPLIT_FARE_DECLINED");
      dataMap.put(ALERT, "SPLIT FARE DECLINED");
    }
    dataMap.put("targetUser", targetUser.getFullName());
    if (targetUser.getPhotoUrl() != null) {
      dataMap.put("targetUserPhoto", targetUser.getPhotoUrl());
    }
    dataMap.put(RIDE_ID, rideId.toString());
    dataMap.put("splitFareId", splitFareId.toString());
    dataMap.put(SOUND, DEFAULT_SOUND);
    pushNotificationToRider(sourceUser, dataMap);
  }

  public void pushRideUpgradeRequest(long rideId, User user, String source, String target, BigDecimal surgeFactor) {
    Map<String, String> dataMap = new HashMap<>(ImmutableMap.<String, String>builder()
      .put(EVENT_KEY, "RIDE_UPGRADE")
      .put(ALERT, String.format("Do you confirm an upgrade to %s class?", target))
      .put(RIDE_ID, String.valueOf(rideId))
      .put("source", source)
      .put("target", target)
      .put("surgeFactor", surgeFactor.toString())
      .put(SOUND, DEFAULT_SOUND)
      .build());

    pushNotificationToRider(user, dataMap);
  }

  public void pushRiderPaymentUpdate(User user, PaymentStatus paymentStatus, Session session) {
    Map<String, String> dataMap = new HashMap<>(ImmutableMap.<String, String>builder()
      .put(EVENT_KEY, "PAYMENT_STATUS_CHANGED")
      .put("paymentStatus", paymentStatus.name())
      .put("minimalVersion", "3.7.0")
      .put("targetPlatform", "ios")
      .put(SOUND, DEFAULT_SOUND)
      .build());
    pushNotificationToRider(user, dataMap, session);
  }

  private List<Token> getTokens(User user) {
    List<Token> tokens = tokenRepository.findByUserAndAvatarType(user, AvatarType.RIDER);
    if (tokens.isEmpty()) {
      return tokens;
    } else {
      return tokens.subList(0, 1);
    }
  }

  public void pushTextNotification(User user, AvatarType avatarType, String message) {
    List<Token> tokens = tokenRepository.findByUserAndAvatarType(user, avatarType);
    getPushNotificationService().publishNotification(tokens, new HashMap<>(ImmutableMap.of(ALERT, message, SOUND, DEFAULT_SOUND)));
  }

  public String subscribeToken(Token token) throws ServerError {
    return getPushNotificationService().subscribeToken(token).getArn();
  }

  public Long deriveApplicationId(Token token) {
    return getPushNotificationService().deriveApplicationId(token);
  }

  public void unsubscribeFromTopics(Token token) {
    getPushNotificationService().unsubscribeFromTopics(token);
  }

  protected void pushNotificationToRider(User user, Map<String, String> dataMap) {
    List<Token> tokens = getTokens(user);
    for (Token token : tokens) {
      getPushNotificationService().publishNotification(Collections.singletonList(token), dataMap);
    }
  }

  private void pushNotificationToRider(User user, Map<String, String> dataMap, Session session) {
    List<Token> tokens = getTokens(user);
    for (Token token : tokens) {
      getPushNotificationService().publishNotification(Collections.singletonList(token), dataMap, session);
    }
  }

  /**
   * Phase out that method when Parse is no longer used.
   * or then we no longer have android versions in the market that are dependent on parse.com
   * than we will always use Amazon SNS notifications
   */
  private PushNotificationsService getPushNotificationService() {
    return notificationsService;
  }

}
