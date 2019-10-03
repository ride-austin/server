package com.rideaustin.clients.configuration;

import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import com.rideaustin.model.ConfigurationItem;
import com.rideaustin.repo.dsl.ConfigurationItemDslRepository;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.env.Environment;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ConfigurationItemServiceTest {

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Mock
  private ConfigurationItemDslRepository configurationItemDslRepository;
  @Mock
  private ObjectMapper objectMapper;
  @Mock
  private Environment environment;
  @Mock
  private ApplicationEventPublisher publisher;

  private ConfigurationItemService testedInstance;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
    testedInstance = new ConfigurationItemService(configurationItemDslRepository, environment, objectMapper, publisher);
  }

  @Test
  public void testUpdateSuccess_for_object_value() throws Exception {
    ConfigurationItem item = new ConfigurationItem();

    Map<String, Object> value = ImmutableMap.of("test", "value");
    when(objectMapper.writeValueAsString(eq(value))).thenReturn("{\"test\":\"value\"}");

    boolean result = testedInstance.update(item, value);

    verify(objectMapper, times(1)).writeValueAsString(eq(value));
    assertEquals("{\"test\":\"value\"}", item.getConfigurationValue());
    verify(configurationItemDslRepository, times(1)).saveAny(eq(item));
    assertTrue(result);
  }

  @Test
  public void testUpdateFailure() throws JsonProcessingException {
    ConfigurationItem item = new ConfigurationItem();
    Map<String, Object> value = ImmutableMap.of("test", "value");

    when(objectMapper.writeValueAsString(eq(value))).thenThrow(new JsonMappingException(""));
    expectedException.expect(IllegalArgumentException.class);

    testedInstance.update(item, value);

    verify(configurationItemDslRepository, never()).saveAny(eq(item));
  }

}