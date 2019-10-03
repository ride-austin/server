package com.rideaustin.service.strategy;

import java.util.Optional;
import java.util.Set;

import javax.inject.Inject;

import org.springframework.stereotype.Component;

import com.google.maps.model.LatLng;
import com.rideaustin.model.Campaign;
import com.rideaustin.model.CampaignArea;
import com.rideaustin.model.enums.CampaignAreaType.SubType;

@Component
public class NoDriveThroughStrategy extends PickupAndDropoffMatchStrategy {

  @Inject
  public NoDriveThroughStrategy(PickupMatchStrategy pickupMatchStrategy, DropoffMatchStrategy dropoffMatchStrategy) {
    super(pickupMatchStrategy, dropoffMatchStrategy);
  }

  @Override
  protected boolean doCheck(LatLng startLocation, LatLng endLocation, Campaign campaign, boolean dropoffMatch) {
    CampaignArea pickup = findCampaignArea(campaign.getPickupZones(), startLocation).orElse(null);
    CampaignArea dropoff = findCampaignArea(campaign.getDropoffZones(), endLocation).orElse(null);
    return super.doCheck(startLocation, endLocation, campaign, dropoffMatch)
      && pickup != null
      && dropoff != null
      && !(SubType.BUS_STOP.equals(pickup.getSubType()) && SubType.BUS_STOP.equals(dropoff.getSubType()));
  }

  private Optional<CampaignArea> findCampaignArea(Set<CampaignArea> areas, LatLng location) {
    return areas.stream()
      .filter(a -> a.contains(location))
      .findFirst();
  }
}
