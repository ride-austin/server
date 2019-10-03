package com.rideaustin.service.strategy;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.google.maps.model.LatLng;
import com.rideaustin.model.Campaign;
import com.rideaustin.model.CampaignArea;

public class DropoffMatchStrategyTest {

  @Mock
  private Campaign campaign;
  @Mock
  private CampaignArea dropoffArea;

  private DropoffMatchStrategy testedInstance;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);

    testedInstance = new DropoffMatchStrategy();
  }

  @Test
  public void isNotEligibleWhenEndLocationIsNull() {
    final boolean result = testedInstance.isEligible(new LatLng(34.061698, -97.6881091), null, null, null);

    assertFalse(result);
  }

  @Test
  public void isNotEligibleWhenEndLocationOutOfDropoffZone() {
    when(campaign.getDropoffZones()).thenReturn(Collections.singleton(dropoffArea));
    when(dropoffArea.contains(any(LatLng.class))).thenReturn(false);

    final boolean result = testedInstance.isEligible(new LatLng(34.061698, -97.6881091), new LatLng(34.94196, -97.98191), null, campaign);

    assertFalse(result);
  }

  @Test
  public void isEligibleWhenEndLocationWithinDropoffZone() {
    when(campaign.getDropoffZones()).thenReturn(Collections.singleton(dropoffArea));
    when(dropoffArea.contains(any(LatLng.class))).thenReturn(true);

    final boolean result = testedInstance.isEligible(new LatLng(34.061698, -97.6881091), new LatLng(34.94196, -97.98191), null, campaign);

    assertTrue(result);
  }
}