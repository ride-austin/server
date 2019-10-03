package com.rideaustin.service.notifications.impl;

import java.util.Map;

import javax.inject.Inject;

import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import com.rideaustin.model.Token;

@Service
public class AmazonPushNotificationsBuilder {

  private final ObjectMapper objectMapper;
  private final Map<String, String> applicationType;

  @Inject
  public AmazonPushNotificationsBuilder(Environment environment, ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
    applicationType = ImmutableMap.<String, String>builder()
      .put("APPLE", environment.getProperty("ra.aws.mobile.push.ios.application_type"))
      .put("GOOGLE", environment.getProperty("ra.aws.mobile.push.android.application_type"))
      .build();
  }

  public String buildPayload(Token t, Map<String, String> message) throws JsonProcessingException {
    Map<String, Object> transformedMessage = transformMessageToPlatformSpecific(t, message);
    Map<String, String> payload = ImmutableMap.of(getAppType(t), objectMapper.writeValueAsString(transformedMessage));
    return objectMapper.writeValueAsString(payload);
  }

  private Map<String, Object> transformMessageToPlatformSpecific(Token t, Map<String, String> message) throws JsonProcessingException {
    return t.getType().transform(message, objectMapper);
  }

  private String getAppType(Token t) {
    return applicationType.get(t.getType().name());
  }
}
