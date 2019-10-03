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

public class PickupMatchStrategyTest {

  @Mock
  private Campaign campaign;
  @Mock
  private CampaignArea pickupArea;

  private PickupMatchStrategy testedInstance;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);

    testedInstance = new PickupMatchStrategy();
  }

  @Test
  public void isNotEligibleWhenStartLocationOutOfPickupZone() {
    when(campaign.getPickupZones()).thenReturn(Collections.singleton(pickupArea));
    when(pickupArea.contains(any(LatLng.class))).thenReturn(false);

    final boolean result = testedInstance.isEligible(new LatLng(34.061698, -97.6881091), new LatLng(34.94196, -97.98191), null, campaign);

    assertFalse(result);
  }

  @Test
  public void isEligibleWhenStartLocationWithinPickupZone() {
    when(campaign.getPickupZones()).thenReturn(Collections.singleton(pickupArea));
    when(pickupArea.contains(any(LatLng.class))).thenReturn(true);

    final boolean result = testedInstance.isEligible(new LatLng(34.061698, -97.6881091), new LatLng(34.94196, -97.98191), null, campaign);

    assertTrue(result);
  }
}