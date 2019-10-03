package com.rideaustin.service.strategy;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.google.maps.model.LatLng;
import com.rideaustin.model.Campaign;
import com.rideaustin.model.user.Rider;

public class UserAndPickupOrDropoffMatchStrategyTest {

  @Mock
  private PickupMatchStrategy pickupMatchStrategy;
  @Mock
  private DropoffMatchStrategy dropoffMatchStrategy;
  @Mock
  private Campaign campaign;

  private UserAndPickupOrDropoffMatchStrategy testedInstance;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);

    testedInstance = new UserAndPickupOrDropoffMatchStrategy(pickupMatchStrategy, dropoffMatchStrategy);
  }

  @Test
  public void isNotEligibleWhenRiderIsNotSubscribed() {
    when(campaign.supportsRider(any(Rider.class))).thenReturn(false);

    final boolean result = testedInstance.isEligible(new LatLng(34.64861, -97.68461), new LatLng(34.65161, -97.68166),
      new Rider(), campaign);

    assertFalse(result);
  }

  @Test
  public void isNotEligibleWhenPickupAndDropoffAreIneligible() {
    when(campaign.supportsRider(any(Rider.class))).thenReturn(true);
    when(pickupMatchStrategy.isEligible(any(LatLng.class), any(LatLng.class), any(Rider.class), eq(campaign))).thenReturn(false);
    when(dropoffMatchStrategy.isEligible(any(LatLng.class), any(LatLng.class), any(Rider.class), eq(campaign))).thenReturn(false);

    final boolean result = testedInstance.isEligible(new LatLng(34.64861, -97.68461), new LatLng(34.65161, -97.68166),
      new Rider(), campaign);

    assertFalse(result);
  }

  @Test
  public void isEligibleWhenEitherPickupOrDropoffIsEligible1() {
    when(campaign.supportsRider(any(Rider.class))).thenReturn(true);
    when(pickupMatchStrategy.isEligible(any(LatLng.class), any(LatLng.class), any(Rider.class), eq(campaign))).thenReturn(true);
    when(dropoffMatchStrategy.isEligible(any(LatLng.class), any(LatLng.class), any(Rider.class), eq(campaign))).thenReturn(false);

    final boolean result = testedInstance.isEligible(new LatLng(34.64861, -97.68461), new LatLng(34.65161, -97.68166),
      new Rider(), campaign);

    assertTrue(result);
  }

  @Test
  public void isEligibleWhenEitherPickupOrDropoffIsEligible2() {
    when(campaign.supportsRider(any(Rider.class))).thenReturn(true);
    when(pickupMatchStrategy.isEligible(any(LatLng.class), any(LatLng.class), any(Rider.class), eq(campaign))).thenReturn(false);
    when(dropoffMatchStrategy.isEligible(any(LatLng.class), any(LatLng.class), any(Rider.class), eq(campaign))).thenReturn(true);

    final boolean result = testedInstance.isEligible(new LatLng(34.64861, -97.68461), new LatLng(34.65161, -97.68166),
      new Rider(), campaign);

    assertTrue(result);
  }
}