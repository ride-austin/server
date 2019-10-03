package com.rideaustin.service.strategy;

import org.springframework.stereotype.Component;

import com.google.maps.model.LatLng;
import com.rideaustin.model.Campaign;
import com.rideaustin.model.CampaignArea;
import com.rideaustin.model.user.Rider;

@Component
public class DropoffMatchStrategy implements CampaignEligibilityStrategy {
  @Override
  public boolean isEligible(LatLng startLocation, LatLng endLocation, Rider rider, Campaign campaign) {
    if (endLocation != null) {
      for (CampaignArea dropoffZone : campaign.getDropoffZones()) {
        if (dropoffZone.contains(endLocation)) {
          return true;
        }
      }
    }
    return false;
  }
}
