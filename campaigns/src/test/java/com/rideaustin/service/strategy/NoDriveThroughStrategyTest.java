package com.rideaustin.service.strategy;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
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
import com.rideaustin.model.enums.CampaignAreaType.SubType;
import com.rideaustin.model.surgepricing.AreaGeometry;
import com.rideaustin.model.user.Rider;

public class NoDriveThroughStrategyTest {

  @Mock
  private PickupMatchStrategy pickupMatchStrategy;
  @Mock
  private DropoffMatchStrategy dropoffMatchStrategy;
  @Mock
  private Campaign campaign;
  @Mock
  private CampaignArea pickupArea;
  @Mock
  private CampaignArea dropoffArea;
  @Mock
  private AreaGeometry pickupZone;
  @Mock
  private AreaGeometry dropoffZone;

  private NoDriveThroughStrategy testedInstance;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);

    testedInstance = new NoDriveThroughStrategy(pickupMatchStrategy, dropoffMatchStrategy);
  }

  @Test
  public void isIneligibleWhenPickupDoesntMatch() {
    when(pickupMatchStrategy.isEligible(any(LatLng.class), any(LatLng.class), any(Rider.class), any(Campaign.class)))
      .thenReturn(false);

    final boolean result = testedInstance.isEligible(new LatLng(34.681681, -97.68161), new LatLng(34.98191, -97.9191), null, null);

    assertFalse(result);
  }

  @Test
  public void isEligibleWhenPickupMatchAndDropoffIsNull() {
    when(pickupMatchStrategy.isEligible(any(LatLng.class), any(LatLng.class), any(Rider.class), any(Campaign.class)))
      .thenReturn(true);

    final boolean result = testedInstance.isEligible(new LatLng(34.681681, -97.68161), null, null, null);

    assertTrue(result);
  }

  @Test
  public void isIneligibleWhenDropoffDoesntMatch() {
    when(pickupMatchStrategy.isEligible(any(LatLng.class), any(LatLng.class), any(Rider.class), any(Campaign.class)))
      .thenReturn(true);
    when(dropoffMatchStrategy.isEligible(any(LatLng.class), any(LatLng.class), any(Rider.class), any(Campaign.class)))
      .thenReturn(false);
    when(campaign.getPickupZones()).thenReturn(Collections.singleton(pickupArea));
    when(campaign.getDropoffZones()).thenReturn(Collections.singleton(dropoffArea));

    final boolean result = testedInstance.isEligible(new LatLng(34.681681, -97.68161), new LatLng(34.98191, -97.9191), null, campaign);

    assertFalse(result);
  }

  @Test
  public void isIneligibleWhenPickupIsSameToDropoff() {
    when(pickupMatchStrategy.isEligible(any(LatLng.class), any(LatLng.class), any(Rider.class), any(Campaign.class)))
      .thenReturn(true);
    when(dropoffMatchStrategy.isEligible(any(LatLng.class), any(LatLng.class), any(Rider.class), any(Campaign.class)))
      .thenReturn(true);
    when(campaign.getPickupZones()).thenReturn(Collections.singleton(pickupArea));
    when(campaign.getDropoffZones()).thenReturn(Collections.singleton(dropoffArea));
    when(pickupArea.getArea()).thenReturn(pickupZone);
    when(dropoffArea.getArea()).thenReturn(pickupZone);

    final boolean result = testedInstance.isEligible(new LatLng(34.681681, -97.68161), new LatLng(34.98191, -97.9191), null, campaign);

    assertFalse(result);
  }

  @Test
  public void isIneligibleWhenPickupAndDropoffAreBothBusStops() {
    when(pickupMatchStrategy.isEligible(any(LatLng.class), any(LatLng.class), any(Rider.class), any(Campaign.class)))
      .thenReturn(true);
    when(dropoffMatchStrategy.isEligible(any(LatLng.class), any(LatLng.class), any(Rider.class), any(Campaign.class)))
      .thenReturn(true);
    when(campaign.getPickupZones()).thenReturn(Collections.singleton(pickupArea));
    when(pickupArea.contains(any(LatLng.class))).thenReturn(true);
    when(campaign.getDropoffZones()).thenReturn(Collections.singleton(dropoffArea));
    when(dropoffArea.contains(any(LatLng.class))).thenReturn(true);
    when(pickupArea.getArea()).thenReturn(pickupZone);
    when(pickupArea.getSubType()).thenReturn(SubType.BUS_STOP);
    when(dropoffArea.getArea()).thenReturn(dropoffZone);
    when(dropoffArea.getSubType()).thenReturn(SubType.BUS_STOP);

    final boolean result = testedInstance.isEligible(new LatLng(34.681681, -97.68161), new LatLng(34.98191, -97.9191), null, campaign);

    assertFalse(result);
  }

  @Test
  public void isEligibleWhenPickupDiffersFromDropoff() {
    when(pickupMatchStrategy.isEligible(any(LatLng.class), any(LatLng.class), any(Rider.class), any(Campaign.class)))
      .thenReturn(true);
    when(dropoffMatchStrategy.isEligible(any(LatLng.class), any(LatLng.class), any(Rider.class), any(Campaign.class)))
      .thenReturn(true);
    when(campaign.getPickupZones()).thenReturn(Collections.singleton(pickupArea));
    when(pickupArea.contains(any(LatLng.class))).thenReturn(true);
    when(campaign.getDropoffZones()).thenReturn(Collections.singleton(dropoffArea));
    when(dropoffArea.contains(any(LatLng.class))).thenReturn(true);
    when(pickupArea.getArea()).thenReturn(pickupZone);
    when(pickupArea.getSubType()).thenReturn(SubType.BUS_STOP);
    when(dropoffArea.getArea()).thenReturn(dropoffZone);
    when(dropoffArea.getSubType()).thenReturn(SubType.AREA);

    final boolean result = testedInstance.isEligible(new LatLng(34.681681, -97.68161), new LatLng(34.98191, -97.9191), null, campaign);

    assertTrue(result);
  }
}