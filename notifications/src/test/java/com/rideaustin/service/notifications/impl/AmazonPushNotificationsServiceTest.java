package com.rideaustin.service.notifications.impl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.beans.factory.BeanFactory;

import com.amazonaws.services.sns.AmazonSNSClient;
import com.amazonaws.services.sns.model.CreatePlatformEndpointRequest;
import com.amazonaws.services.sns.model.CreatePlatformEndpointResult;
import com.amazonaws.services.sns.model.SubscribeRequest;
import com.amazonaws.services.sns.model.SubscribeResult;
import com.amazonaws.services.sns.model.UnsubscribeRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rideaustin.model.ConfigurationItem;
import com.rideaustin.clients.configuration.ConfigurationItemService;
import com.rideaustin.filter.ClientAgentCity;
import com.rideaustin.model.Token;
import com.rideaustin.model.enums.AvatarType;
import com.rideaustin.repo.jpa.TokenRepository;
import com.rideaustin.rest.exception.ServerError;
import com.rideaustin.service.notifications.ApplicationPolicy;
import com.rideaustin.service.notifications.SubscriptionPolicy;
import com.rideaustin.service.notifications.applications.AustinDriverIosApplicationPolicy;
import com.rideaustin.service.notifications.applications.AustinRiderIosApplicationPolicy;
import com.rideaustin.service.notifications.applications.HoustonDriverIosApplicationPolicy;
import com.rideaustin.service.notifications.applications.HoustonRiderIosApplicationPolicy;
import com.rideaustin.service.notifications.subscriptions.AustinDriversPolicy;
import com.rideaustin.service.notifications.subscriptions.AustinRidersPolicy;
import com.rideaustin.service.notifications.subscriptions.HoustonDriversPolicy;
import com.rideaustin.service.notifications.subscriptions.HoustonRidersPolicy;

@RunWith(MockitoJUnitRunner.class)
public class AmazonPushNotificationsServiceTest {

  private static final String topics = "[{\"id\":1,\"name\":\"Austin Riders\",\"description\":\"Austin Riders Subscription\",\"arn\":\"arn:aws:sns:us-east-1:365685046369:RideAllRidersDev\",\"subscriptionPolicyClassName\":\"austinRidersPolicy\"},\n" +
    "   {\"id\":2,\"name\":\"Austin Drivers\",\"description\":\"Austin Drivers Subscription\",\"arn\":\"arn:aws:sns:us-east-1:365685046369:RideAustinDriversDev\",\"subscriptionPolicyClassName\":\"austinDriversPolicy\"},\n" +
    "   {\"id\":3,\"name\":\"Houston Riders\",\"description\":\"Houston Riders Subscription\",\"arn\":\"arn:aws:sns:us-east-1:365685046369:RideHoustonRidersDev\",\"subscriptionPolicyClassName\":\"houstonRidersPolicy\"},\n" +
    "   {\"id\":4,\"name\":\"Houston Drivers\",\"description\":\"Houston Drivers Subscription\",\"arn\":\"arn:aws:sns:us-east-1:365685046369:RideHoustonDriversDev\",\"subscriptionPolicyClassName\":\"houstonDriversPolicy\"}]";

  private static final String applications = "[{\"id\":1,\"arn\":\"arn:aws:sns:us-east-1:365685046369:app/APNS_SANDBOX/RideDev\",\"applicationPolicyClassName\":\"austinRiderIosApplicationPolicy\"},\n" +
    "   {\"id\":2,\"arn\":\"arn:aws:sns:us-east-1:365685046369:app/APNS_SANDBOX/RideDriverAustinDev\",\"applicationPolicyClassName\":\"austinDriverIosApplicationPolicy\"},\n" +
    "   {\"id\":3,\"arn\":\"arn:aws:sns:us-east-1:365685046369:app/APNS_SANDBOX/RideHoustonDev\",\"applicationPolicyClassName\":\"houstonRiderIosApplicationPolicy\"},\n" +
    "   {\"id\":4,\"arn\":\"arn:aws:sns:us-east-1:365685046369:app/APNS_SANDBOX/RideDriverHoustonDev\",\"applicationPolicyClassName\":\"houstonDriverIosApplicationPolicy\"}]";
  public static final String TOKEN_VALUE = "token_value";
  public static final String ENDPOINT_ARN = "endpoint_arn";
  @Mock
  private AmazonSNSClient amazonSNSClient;
  @Mock
  private TokenRepository tokenRepository;
  @Mock
  private AmazonPushNotificationsBuilder amazonPushNotificationsBuilder;
  @Mock
  private ConfigurationItemService configurationItemService;
  @Mock
  private BeanFactory beanFactory;

  private ObjectMapper objectMapper;

  private AmazonPushNotificationsService amazonPushNotificationsService;

  @Before
  public void setup() throws ServerError {
    objectMapper = new ObjectMapper();

    ConfigurationItem itemTopics = mockConfigurationItem(topics);
    ConfigurationItem itemApplications = mockConfigurationItem(applications);

    when(configurationItemService.findByKey(AmazonPushNotificationsService.SUBSCRIPTION_TOPICS))
      .thenReturn(itemTopics);
    when(configurationItemService.findByKey(AmazonPushNotificationsService.SNS_APPLICATION))
      .thenReturn(itemApplications);

    when(beanFactory.getBean("austinRidersPolicy", SubscriptionPolicy.class)).thenReturn(new AustinRidersPolicy());
    when(beanFactory.getBean("austinDriversPolicy", SubscriptionPolicy.class)).thenReturn(new AustinDriversPolicy());
    when(beanFactory.getBean("houstonRidersPolicy", SubscriptionPolicy.class)).thenReturn(new HoustonRidersPolicy());
    when(beanFactory.getBean("houstonDriversPolicy", SubscriptionPolicy.class)).thenReturn(new HoustonDriversPolicy());

    when(beanFactory.getBean("austinRiderIosApplicationPolicy", ApplicationPolicy.class)).thenReturn(new AustinRiderIosApplicationPolicy());
    when(beanFactory.getBean("austinDriverIosApplicationPolicy", ApplicationPolicy.class)).thenReturn(new AustinDriverIosApplicationPolicy());
    when(beanFactory.getBean("houstonRiderIosApplicationPolicy", ApplicationPolicy.class)).thenReturn(new HoustonRiderIosApplicationPolicy());
    when(beanFactory.getBean("houstonDriverIosApplicationPolicy", ApplicationPolicy.class)).thenReturn(new HoustonDriverIosApplicationPolicy());

    CreatePlatformEndpointResult result = new CreatePlatformEndpointResult();
    result.setEndpointArn(ENDPOINT_ARN);
    when(amazonSNSClient.createPlatformEndpoint(any(CreatePlatformEndpointRequest.class))).thenReturn(result);

    when(amazonSNSClient.subscribe(any())).thenReturn(new SubscribeResult().withSubscriptionArn("topicSubscriptionArn"));

    amazonPushNotificationsService
      = new AmazonPushNotificationsService(amazonSNSClient, tokenRepository, amazonPushNotificationsBuilder,
      configurationItemService, objectMapper, beanFactory);

  }

  @Test
  public void testDeriveApplicationIdTestNull() throws Exception {
    Token token = mockToken(ClientAgentCity.AUSTIN, AvatarType.RIDER, Token.TokenType.GOOGLE, 1L);
    Long cityId = amazonPushNotificationsService.deriveApplicationId(token);
    assertThat(cityId, is(nullValue()));
  }

  @Test
  public void testDeriveApplicationIdTest1() throws Exception {
    Token token = mockToken(ClientAgentCity.AUSTIN, AvatarType.RIDER, Token.TokenType.APPLE, 1L);
    Long cityId = amazonPushNotificationsService.deriveApplicationId(token);
    assertThat(cityId, equalTo(1L));
  }

  @Test
  public void testDeriveApplicationIdTest2() throws Exception {
    Token token = mockToken(ClientAgentCity.AUSTIN, AvatarType.DRIVER, Token.TokenType.APPLE, 1L);
    Long cityId = amazonPushNotificationsService.deriveApplicationId(token);
    assertThat(cityId, equalTo(2L));
  }

  @Test
  public void testDeriveApplicationIdTest3() throws Exception {
    Token token = mockToken(ClientAgentCity.HOUSTON, AvatarType.RIDER, Token.TokenType.APPLE, 1L);
    Long cityId = amazonPushNotificationsService.deriveApplicationId(token);
    assertThat(cityId, equalTo(3L));
  }

  @Test
  public void testDeriveApplicationIdTest4() throws Exception {
    Token token = mockToken(ClientAgentCity.HOUSTON, AvatarType.DRIVER, Token.TokenType.APPLE, 1L);
    Long cityId = amazonPushNotificationsService.deriveApplicationId(token);
    assertThat(cityId, equalTo(4L));
  }

  @Test
  public void testSubscribeToken1() throws Exception {
    Token token = mockToken(ClientAgentCity.AUSTIN, AvatarType.RIDER, Token.TokenType.APPLE, 1L);
    String arn = amazonPushNotificationsService.subscribeToken(token).getArn();

    captureAndValidateSubscriptionResult(arn, "arn:aws:sns:us-east-1:365685046369:RideAllRidersDev", token);
  }

  @Test
  public void testSubscribeToken2() throws Exception {
    Token token = mockToken(ClientAgentCity.AUSTIN, AvatarType.DRIVER, Token.TokenType.APPLE, 2L);
    String arn = amazonPushNotificationsService.subscribeToken(token).getArn();

    captureAndValidateSubscriptionResult(arn, "arn:aws:sns:us-east-1:365685046369:RideAustinDriversDev", token);

  }

  @Test
  public void testSubscribeToken3() throws Exception {
    Token token = mockToken(ClientAgentCity.HOUSTON, AvatarType.RIDER, Token.TokenType.APPLE, 3L);
    String arn = amazonPushNotificationsService.subscribeToken(token).getArn();

    captureAndValidateSubscriptionResult(arn, "arn:aws:sns:us-east-1:365685046369:RideHoustonRidersDev", token);

  }

  @Test
  public void testSubscribeToken4() throws Exception {
    Token token = mockToken(ClientAgentCity.HOUSTON, AvatarType.DRIVER, Token.TokenType.APPLE, 4L);
    String arn = amazonPushNotificationsService.subscribeToken(token).getArn();

    captureAndValidateSubscriptionResult(arn, "arn:aws:sns:us-east-1:365685046369:RideHoustonDriversDev", token);

  }

  @Test
  public void testUnsubscribeFromTopics() {
    final String topicSubscriptions = "topicSubscriptionArn1";
    Token token = mockToken(ClientAgentCity.AUSTIN, AvatarType.RIDER, Token.TokenType.APPLE, 1L);
    token.setTopicSubscriptions(topicSubscriptions);

    amazonPushNotificationsService.unsubscribeFromTopics(token);

    verify(amazonSNSClient, times(1)).unsubscribe(any(UnsubscribeRequest.class));
  }

  @Test
  public void testUnsubscribeFromTopics_WithMultipleTopics() {
    final String topicSubscriptions = "topicSubscriptionArn1,TopicSubscriptionArn2";
    Token token = mockToken(ClientAgentCity.AUSTIN, AvatarType.RIDER, Token.TokenType.APPLE, 1L);
    token.setTopicSubscriptions(topicSubscriptions);

    amazonPushNotificationsService.unsubscribeFromTopics(token);

    verify(amazonSNSClient, times(2)).unsubscribe(any(UnsubscribeRequest.class));
  }

  private void captureAndValidateSubscriptionResult(String arn, String topicArn, Token token) {
    ArgumentCaptor<CreatePlatformEndpointRequest> createRequest = ArgumentCaptor.forClass(CreatePlatformEndpointRequest.class);
    verify(amazonSNSClient).createPlatformEndpoint(createRequest.capture());

    ArgumentCaptor<SubscribeRequest> subscribeRequest = ArgumentCaptor.forClass(SubscribeRequest.class);
    verify(amazonSNSClient).subscribe(subscribeRequest.capture());

    assertThat(createRequest.getValue().getToken(), is(TOKEN_VALUE));
    assertThat(subscribeRequest.getValue().getEndpoint(), is(arn));
    assertThat(subscribeRequest.getValue().getTopicArn(), is(topicArn));
    assertThat(token.getTopicSubscriptions(), is("topicSubscriptionArn"));
  }

  private Token mockToken(ClientAgentCity cityAgent, AvatarType avatar, Token.TokenType type, long appId) {
    Token token = new Token();
    token.setAgent(cityAgent);
    token.setAvatarType(avatar);
    token.setType(type);
    token.setValue(TOKEN_VALUE);
    token.setApplicationId(appId);
    return token;
  }

  private ConfigurationItem mockConfigurationItem(String value) {
    ConfigurationItem item = new ConfigurationItem();
    item.setConfigurationValue(value);
    return item;
  }

}