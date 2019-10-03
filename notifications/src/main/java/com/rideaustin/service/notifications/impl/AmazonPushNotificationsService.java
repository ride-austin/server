package com.rideaustin.service.notifications.impl;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import com.amazonaws.services.sns.AmazonSNSClient;
import com.amazonaws.services.sns.model.CreatePlatformEndpointRequest;
import com.amazonaws.services.sns.model.CreatePlatformEndpointResult;
import com.amazonaws.services.sns.model.EndpointDisabledException;
import com.amazonaws.services.sns.model.InvalidParameterException;
import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sns.model.SetEndpointAttributesRequest;
import com.amazonaws.services.sns.model.SubscribeRequest;
import com.amazonaws.services.sns.model.UnsubscribeRequest;
import com.amazonaws.services.sns.model.UnsubscribeResult;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.rideaustin.model.ConfigurationItem;
import com.rideaustin.clients.configuration.ConfigurationItemService;
import com.rideaustin.model.Session;
import com.rideaustin.model.Token;
import com.rideaustin.model.enums.AvatarType;
import com.rideaustin.repo.jpa.TokenRepository;
import com.rideaustin.rest.exception.ServerError;
import com.rideaustin.service.model.Version;
import com.rideaustin.service.notifications.ApplicationPolicy;
import com.rideaustin.service.notifications.PushNotificationsService;
import com.rideaustin.service.notifications.SubscriptionPolicy;
import com.rideaustin.service.notifications.model.Application;
import com.rideaustin.service.notifications.model.Topic;
import com.rideaustin.utils.AppInfoUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Primary
@DependsOn(value = {
  "houstonRidersPolicy", "houstonDriversPolicy",
  "austinRidersPolicy", "austinDriversPolicy",
  "austinRiderIosApplicationPolicy", "austinDriverIosApplicationPolicy",
  "houstonRiderIosApplicationPolicy", "houstonDriverIosApplicationPolicy"})
public class AmazonPushNotificationsService implements PushNotificationsService {

  static final String SUBSCRIPTION_TOPICS = "subscriptionTopics";
  static final String SNS_APPLICATION = "snsApplications";
  private static final String TOPIC_SUBSCRIPTION_SEPARATOR = ",";

  private final AmazonSNSClient amazonSNSClient;
  private final TokenRepository tokenRepository;
  private final AmazonPushNotificationsBuilder amazonPushNotificationsBuilder;

  private final ConfigurationItemService configurationItemService;
  private final ObjectMapper objectMapper;
  private final BeanFactory beanFactory;
  private final Pattern existingEndpointPattern =
    Pattern.compile(".*Endpoint (arn:aws:sns[^ ]+) already exists with the same token.*");

  private Map<Long, Application> applicationArnMap;
  private Map<Long, Topic> subscriptionTopics;

  @Inject
  public AmazonPushNotificationsService(AmazonSNSClient amazonSNSClient,
    TokenRepository tokenRepository, AmazonPushNotificationsBuilder amazonPushNotificationsBuilder,
    ConfigurationItemService configurationItemService, ObjectMapper objectMapper,
    BeanFactory beanFactory) throws ServerError {

    this.amazonSNSClient = amazonSNSClient;
    this.tokenRepository = tokenRepository;
    this.amazonPushNotificationsBuilder = amazonPushNotificationsBuilder;

    this.configurationItemService = configurationItemService;
    this.objectMapper = objectMapper;
    this.beanFactory = beanFactory;
    try {
      this.subscriptionTopics = loadSubscriptionTopics();
      this.applicationArnMap = loadApplications();
    } catch (IOException e) {
      throw new ServerError("Unable to load application configuration", e);
    }

  }

  @Override
  public SubscribeResult subscribeToken(Token token) throws ServerError {
    String endpointArn = null;
    boolean success = true;
    try {
      CreatePlatformEndpointRequest cpeReq =
        new CreatePlatformEndpointRequest()
          .withPlatformApplicationArn(getApplicationArn(token))
          .withToken(token.getValue());
      CreatePlatformEndpointResult cpeRes = amazonSNSClient
        .createPlatformEndpoint(cpeReq);
      endpointArn = cpeRes.getEndpointArn();
      token.setArn(endpointArn);
      subscribeToTopics(token);
    } catch (InvalidParameterException ipe) {
      String message = ipe.getErrorMessage();
      Matcher m = existingEndpointPattern.matcher(message);
      if (m.matches()) {
        endpointArn = m.group(1);
      } else {
        success = false;
        List<Token> tokens = tokenRepository.findByUserAndAvatarType(token.getUser(), AvatarType.RIDER);
        for (Token t : tokens) {
          unsubscribeFromTopics(t);
        }
        tokenRepository.delete(tokens);
        log.error("Error while subscribing", ipe);
      }
    }
    return new SubscribeResult(endpointArn, success);
  }

  @Override
  public Long deriveApplicationId(Token token) {
    for (Application application : applicationArnMap.values()) {
      if (application.getPolicy().shouldUseApplication(token)) {
        return application.getId();
      }
    }
    return null;
  }

  @Override
  public void unsubscribeFromTopics(Token token) {
    final String topicSubscriptions = token.getTopicSubscriptions();
    if (StringUtils.isNotBlank(topicSubscriptions)) {
      String[] topicSubscriptionArray = topicSubscriptions.split(TOPIC_SUBSCRIPTION_SEPARATOR);
      for (String topicSubscriptionArn : topicSubscriptionArray) {
        final UnsubscribeResult result = unsubscribeFromTopic(topicSubscriptionArn);
        if (result != null) {
          log.info(String.format("[SNS] Token %s unsubscribed to %s", token.getValue(), topicSubscriptionArn));
        }
      }
    }
  }

  private void subscribeToTopics(Token token) {
    List<String> topicSubscriptionArnList = Lists.newArrayList();
    for (Topic topic : subscriptionTopics.values()) {
      if (topic.getPolicy().shouldSubscribe(token)) {
        com.amazonaws.services.sns.model.SubscribeResult result = subscribeToTopic(topic.getArn(), token.getArn());
        if (result != null) {
          log.info(String.format("[SNS] Token %s subscribed to %s as %s", token.getValue(), topic.getArn(), result.getSubscriptionArn()));
          topicSubscriptionArnList.add(result.getSubscriptionArn());
        }
      }
    }

    token.setTopicSubscriptions(StringUtils.join(topicSubscriptionArnList, ","));
  }

  private String getApplicationArn(Token token) throws ServerError {
    String applicationArn = applicationArnMap.get(token.getApplicationId()).getArn();
    if (applicationArn == null) {
      throw new ServerError("Unsupported client platform");
    }
    return applicationArn;
  }

  private com.amazonaws.services.sns.model.SubscribeResult subscribeToTopic(String topicArn, String endpointArn) {
    SubscribeRequest subscribeRequest =
      new SubscribeRequest()
        .withProtocol("application")
        .withEndpoint(endpointArn).withTopicArn(topicArn);
    return amazonSNSClient.subscribe(subscribeRequest);
  }

  private UnsubscribeResult unsubscribeFromTopic(String topicSubscriptionArn) {
    UnsubscribeRequest unsubscribeRequest = new UnsubscribeRequest()
      .withSubscriptionArn(topicSubscriptionArn);
    return amazonSNSClient.unsubscribe(unsubscribeRequest);
  }

  @Override
  public void publishNotification(List<Token> tokens, Map<String, String> message) {
    publishNotification(tokens, message, null);
  }

  @Override
  public void publishNotification(List<Token> tokens, Map<String, String> message, Session session) {
    try {
      for (Token t : tokens) {
        boolean proceed = true;
        if (StringUtils.isEmpty(t.getArn())) {
          proceed = registerToken(t);
        }
        if (message.containsKey("minimalVersion") && message.containsKey("targetPlatform") && session != null) {
          Version version = AppInfoUtils.createVersion(session.getUserAgent());
          boolean platformMatch = Optional.ofNullable(session.getUserPlatform()).map(String::toLowerCase)
            .map(s -> s.contains(message.get("targetPlatform").toLowerCase()))
            .orElse(false);
          if (platformMatch && version.compareTo(new Version(message.get("minimalVersion"))) < 0) {
            continue;
          }
        }
        String payload = amazonPushNotificationsBuilder.buildPayload(t, message);
        if (proceed) {
          pushNotificationWithRetry(t, payload);
        }
      }
    } catch (JsonProcessingException | ServerError e) {
      log.error("Error sending push notification", e);
    }
  }

  private void pushNotificationWithRetry(Token token, String message) {
    try {
      pushNotification(token.getArn(), message);
    } catch (EndpointDisabledException e) {
      enableEndpoint(token);
      pushNotification(token.getArn(), message);
      log.error("Endpoint disabled", e);
    }
  }

  private void enableEndpoint(Token token) {
    Map<String, String> attributes = ImmutableMap.of("Enabled", "true", "Token", token.getValue());
    SetEndpointAttributesRequest attrRequest
      = new SetEndpointAttributesRequest().withAttributes(attributes).withEndpointArn(token.getArn());
    amazonSNSClient.setEndpointAttributes(attrRequest);
  }

  public void pushTextNotification(Long topicId, String message) {
    Topic topic = subscriptionTopics.get(topicId);
    if (topic != null) {
      PublishRequest publishRequest = new PublishRequest()
        .withMessage(message).withTargetArn(topic.getArn());
      amazonSNSClient.publish(publishRequest);
    }
  }

  private void pushNotification(String arn, String message) {
    PublishRequest publishRequest = new PublishRequest()
      .withMessage(message).withMessageStructure("json").withTargetArn(arn);
    amazonSNSClient.publish(publishRequest);
  }

  private boolean registerToken(Token token) throws ServerError {
    SubscribeResult subscribeResult = subscribeToken(token);
    String arn = subscribeResult.getArn();
    token.setArn(arn);
    if (subscribeResult.isSuccess()) {
      tokenRepository.saveAndFlush(token);
    }
    return subscribeResult.isSuccess();
  }

  public Collection<Topic> listTopics() {
    return subscriptionTopics.values();
  }

  private Map<Long, Topic> loadSubscriptionTopics() throws IOException {
    ConfigurationItem configurationItem = configurationItemService.findByKey(SUBSCRIPTION_TOPICS);
    List<Topic> topics = objectMapper.readValue(configurationItem.getConfigurationValue(), new TypeReference<List<Topic>>() {});
    for (Topic t : topics) {
      t.setPolicy(beanFactory.getBean(t.getSubscriptionPolicyClassName(), SubscriptionPolicy.class));
    }
    return topics.stream().collect(Collectors.toMap(Topic::getId, Function.identity()));
  }

  private Map<Long, Application> loadApplications() throws IOException {
    ConfigurationItem configurationItem = configurationItemService.findByKey(SNS_APPLICATION);
    List<Application> applications = objectMapper.readValue(configurationItem.getConfigurationValue(), new TypeReference<List<Application>>() {});
    for (Application a : applications) {
      a.setPolicy(beanFactory.getBean(a.getApplicationPolicyClassName(), ApplicationPolicy.class));
    }
    return applications.stream().collect(Collectors.toMap(Application::getId, Function.identity()));
  }
}
