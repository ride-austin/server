package com.rideaustin.service.strategy;

import javax.inject.Inject;

import org.springframework.stereotype.Component;

import com.google.maps.model.LatLng;
import com.rideaustin.model.Campaign;
import com.rideaustin.model.user.Rider;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class UserAndPickupOrDropoffMatchStrategy implements CampaignEligibilityStrategy {

  private final PickupMatchStrategy pickupMatchStrategy;
  private final DropoffMatchStrategy dropoffMatchStrategy;

  @Override
  public boolean isEligible(LatLng startLocation, LatLng endLocation, Rider rider, Campaign campaign) {
    return campaign.supportsRider(rider) && (
      pickupMatchStrategy.isEligible(startLocation, endLocation, rider, campaign) ||
        dropoffMatchStrategy.isEligible(startLocation, endLocation, rider, campaign)
    );
  }
}
