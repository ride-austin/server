package com.rideaustin.service;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashSet;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.ImmutableSet;
import com.rideaustin.model.Campaign;
import com.rideaustin.model.CampaignArea;
import com.rideaustin.model.ride.RideTracker;
import com.rideaustin.service.CampaignService.RideTrackAnalyzer;

public class RideTrackAnalyzerTest {

  private RideTrackAnalyzer testedInstance;

  @Before
  public void setUp() throws Exception {
    testedInstance = new RideTrackAnalyzer(1L, 0.7);
  }

  @Test
  public void analyzeForReturnsFalseOnEmptyTrack() {
    final boolean result = testedInstance.analyzeFor(new Campaign());

    assertFalse(result);
  }

  @Test
  public void analyzeForReturnsFalseWhenRideStartedOutsidePickupZone() {
    final RideTracker tracker = new RideTracker();
    tracker.setLatitude(34.981961);
    tracker.setLongitude(-97.984116);
    testedInstance.addTracker(tracker);

    final Campaign campaign = new Campaign();
    final CampaignArea campaignArea = mock(CampaignArea.class);
    when(campaignArea.contains(tracker.getLatitude(), tracker.getLongitude())).thenReturn(false);
    campaign.setPickupZones(ImmutableSet.of(campaignArea));

    final boolean result = testedInstance.analyzeFor(campaign);

    assertFalse(result);
  }

  @Test
  public void analyzeForReturnsFalseWhenTrackersInZoneAreLessThanThreshold() {
    final RideTracker startTracker = new RideTracker();
    startTracker.setLatitude(34.981961);
    startTracker.setLongitude(-97.984116);
    testedInstance.addTracker(startTracker);
    final RideTracker secondTracker = new RideTracker();
    secondTracker.setLatitude(34.984191);
    secondTracker.setLongitude(-97.98461);
    testedInstance.addTracker(secondTracker);
    testedInstance.addTracker(secondTracker);

    final Campaign campaign = new Campaign();
    final CampaignArea campaignArea = mock(CampaignArea.class);
    when(campaignArea.contains(startTracker.getLatitude(), startTracker.getLongitude())).thenReturn(true);
    when(campaignArea.contains(secondTracker.getLatitude(), secondTracker.getLongitude())).thenReturn(false);
    campaign.setPickupZones(ImmutableSet.of(campaignArea));
    campaign.setDropoffZones(new HashSet<>());

    final boolean result = testedInstance.analyzeFor(campaign);

    assertFalse(result);
  }

  @Test
  public void analyzeForReturnsTrueWhenTrackersInZoneAreMoreThanThreshold() {
    final RideTracker startTracker = new RideTracker();
    startTracker.setLatitude(34.981961);
    startTracker.setLongitude(-97.984116);
    testedInstance.addTracker(startTracker);
    final RideTracker secondTracker = new RideTracker();
    secondTracker.setLatitude(34.984191);
    secondTracker.setLongitude(-97.98461);
    testedInstance.addTracker(secondTracker);

    final Campaign campaign = new Campaign();
    final CampaignArea campaignArea = mock(CampaignArea.class);
    when(campaignArea.contains(startTracker.getLatitude(), startTracker.getLongitude())).thenReturn(true);
    when(campaignArea.contains(secondTracker.getLatitude(), secondTracker.getLongitude())).thenReturn(true);
    campaign.setPickupZones(ImmutableSet.of(campaignArea));
    campaign.setDropoffZones(new HashSet<>());

    final boolean result = testedInstance.analyzeFor(campaign);

    assertTrue(result);
  }

}