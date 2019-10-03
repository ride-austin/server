package com.rideaustin.service.strategy;

import org.springframework.stereotype.Component;

import com.google.maps.model.LatLng;
import com.rideaustin.model.Campaign;
import com.rideaustin.model.CampaignArea;
import com.rideaustin.model.user.Rider;

@Component
public class PickupMatchStrategy implements CampaignEligibilityStrategy {

  @Override
  public boolean isEligible(LatLng startLocation, LatLng endLocation, Rider rider, Campaign campaign) {
    for (CampaignArea pickupArea : campaign.getPickupZones()) {
      if (pickupArea.contains(startLocation)) {
        return true;
      }
    }
    return false;
  }
}
