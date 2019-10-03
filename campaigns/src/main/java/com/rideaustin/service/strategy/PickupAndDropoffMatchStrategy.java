package com.rideaustin.service.strategy;

import java.util.Optional;
import java.util.Set;

import javax.inject.Inject;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import com.google.maps.model.LatLng;
import com.rideaustin.model.Campaign;
import com.rideaustin.model.CampaignArea;
import com.rideaustin.model.surgepricing.AreaGeometry;
import com.rideaustin.model.user.Rider;

import lombok.RequiredArgsConstructor;

@Primary
@Component
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class PickupAndDropoffMatchStrategy implements CampaignEligibilityStrategy {

  private final PickupMatchStrategy pickupMatchStrategy;
  private final DropoffMatchStrategy dropoffMatchStrategy;

  @Override
  public boolean isEligible(LatLng startLocation, LatLng endLocation, Rider rider, Campaign campaign) {
    boolean pickupMatch = pickupMatchStrategy.isEligible(startLocation, endLocation, rider, campaign);
    if (pickupMatch && endLocation != null) {
      boolean dropoffMatch = dropoffMatchStrategy.isEligible(startLocation, endLocation, rider, campaign);
      return doCheck(startLocation, endLocation, campaign, dropoffMatch);
    } else {
      return pickupMatch;
    }
  }

  protected boolean doCheck(LatLng startLocation, LatLng endLocation, Campaign campaign, boolean dropoffMatch) {
    AreaGeometry pickup = findArea(campaign.getPickupZones(), startLocation).orElse(null);
    AreaGeometry dropoff = findArea(campaign.getDropoffZones(), endLocation).orElse(null);
    return dropoffMatch && pickup != null && !pickup.equals(dropoff);
  }

  private Optional<AreaGeometry> findArea(Set<CampaignArea> areas, LatLng location) {
    return areas.stream()
      .filter(a -> a.contains(location))
      .findFirst()
      .map(CampaignArea::getArea);
  }
}
