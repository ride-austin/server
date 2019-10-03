package com.rideaustin.clients.configuration.elements;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.core.env.Environment;

import com.rideaustin.filter.ClientType;
import com.rideaustin.rest.model.Location;

public class MaskedPhoneConfigurationElementTest {

  @Mock
  private Environment environment;

  private MaskedPhoneConfigurationElement testedInstance;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);

    testedInstance = new MaskedPhoneConfigurationElement(environment);
  }

  @Test
  public void getConfigurationReturnsConfigured() {
    final String configured = "+151255555555";
    when(environment.getProperty("sms.twilio.sender")).thenReturn(configured);
    final Map result = testedInstance.getConfiguration(ClientType.RIDER, new Location(), 1L);

    assertEquals(1, result.size());
    assertTrue(result.containsKey("directConnectPhone"));
    assertEquals(configured, result.get("directConnectPhone"));
  }
}