package com.rideaustin.service.thirdparty.lookup.condition;

import static com.rideaustin.Constants.Configuration.LOOKUP_SERVICE_MOCK;
import static com.rideaustin.Constants.Configuration.LOOKUP_SERVICE_TWILIO;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.mock.env.MockEnvironment;

@RunWith(MockitoJUnitRunner.class)
public class LookupServiceConditionTest {

  private MockEnvironment environment = new MockEnvironment();

  private MockLookupServiceCondition mockCondition = new MockLookupServiceCondition();

  private TwilioLookupServiceCondition twilioCondition = new TwilioLookupServiceCondition();

  @Mock
  private ConditionContext conditionContext;

  @Mock
  private AnnotatedTypeMetadata metadata;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    environment = new MockEnvironment();
    when(conditionContext.getEnvironment()).thenReturn(environment);
  }

  @Test
  public void shouldEnableMockService_WhenMockIsConfigured() {
    environment.setProperty("lookup.api.default.provider", LOOKUP_SERVICE_MOCK);

    boolean actual = mockCondition.matches(conditionContext, metadata);

    assertThat(actual).isTrue();
  }

  @Test
  public void shouldNotEnableTwilioService_WhenMockIsConfigured() {
    environment.setProperty("lookup.api.default.provider", LOOKUP_SERVICE_MOCK);

    boolean actual = twilioCondition.matches(conditionContext, metadata);

    assertThat(actual).isFalse();
  }

  @Test
  public void shouldEnableTwilioService_WhenTwilioIsConfigured() {
    environment.setProperty("lookup.api.default.provider", LOOKUP_SERVICE_TWILIO);

    boolean actual = twilioCondition.matches(conditionContext, metadata);

    assertThat(actual).isTrue();
  }

  public void shouldNotEnableMockService_WhenTwilioIsConfigured() {
    environment.setProperty("lookup.api.default.provider", LOOKUP_SERVICE_TWILIO);

    boolean actual = mockCondition.matches(conditionContext, metadata);

    assertThat(actual).isFalse();
  }
}
