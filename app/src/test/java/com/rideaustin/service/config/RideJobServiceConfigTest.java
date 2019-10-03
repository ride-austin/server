package com.rideaustin.service.config;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.core.env.Environment;

import com.rideaustin.clients.configuration.ConfigurationItemCache;

public class RideJobServiceConfigTest {

  @Mock
  private ConfigurationItemCache configurationItemCache;
  @Mock
  private Environment environment;

  @InjectMocks
  private RideJobServiceConfig config;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void shouldGetPaymentDelay() {
    // given
    Integer expected = 60;
    when(configurationItemCache.getConfigAsInt(any(), any(), any())).thenReturn(expected);

    // when
    Integer result = config.getRidePaymentDelay();

    // then
    assertEquals(expected, result);
  }
}