package com.rideaustin.service.notifications;

import java.util.List;
import java.util.Map;

import com.rideaustin.model.Session;
import com.rideaustin.model.Token;
import com.rideaustin.rest.exception.ServerError;

import lombok.AllArgsConstructor;
import lombok.Getter;

public interface PushNotificationsService {

  SubscribeResult subscribeToken(Token token) throws ServerError;

  void publishNotification(List<Token> tokens, Map<String, String> dataMap);

  void publishNotification(List<Token> tokens, Map<String, String> dataMap, Session session);

  Long deriveApplicationId(Token token);

  void unsubscribeFromTopics(Token token);

  @Getter
  @AllArgsConstructor
  class SubscribeResult {
    final String arn;
    final boolean success;
  }
}
