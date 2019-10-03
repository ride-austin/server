package com.rideaustin.service.strategy;

import com.google.maps.model.LatLng;
import com.rideaustin.model.Campaign;
import com.rideaustin.model.user.Rider;

@FunctionalInterface
public interface CampaignEligibilityStrategy {

  boolean isEligible(LatLng startLocation, LatLng endLocation, Rider rider, Campaign campaign);
}
