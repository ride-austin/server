package com.rideaustin.clients.configuration.elements;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

import java.util.Date;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.rideaustin.filter.ClientType;
import com.rideaustin.model.TermsInfo;
import com.rideaustin.rest.model.Location;
import com.rideaustin.service.TermsService;

public class TermsConfigurationElementTest {

  @Mock
  private TermsService termsService;

  private TermsConfigurationElement testedInstance;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);

    testedInstance = new TermsConfigurationElement(termsService);
  }

  @Test
  public void getDefaultConfigurationContainsTermsInfo() {
    final long cityId = 1L;
    final TermsInfo terms = new TermsInfo(1L, "A", true, "1.0", new Date());
    when(termsService.getCurrentTerms(cityId)).thenReturn(terms);

    final Map result = testedInstance.getDefaultConfiguration(ClientType.RIDER, new Location(), cityId);

    assertEquals(terms, result.get("currentTerms"));
  }
}