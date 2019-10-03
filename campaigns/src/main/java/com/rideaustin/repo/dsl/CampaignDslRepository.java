package com.rideaustin.repo.dsl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;

import com.rideaustin.config.CacheConfiguration;
import com.rideaustin.model.Campaign;
import com.rideaustin.model.CampaignRider;
import com.rideaustin.model.QCampaign;
import com.rideaustin.model.rest.CampaignRiderDto;
import com.rideaustin.model.ride.Ride;

@Repository
public class CampaignDslRepository extends AbstractDslRepository {

  private static final QCampaign qCampaign = QCampaign.campaign;

  @Cacheable(cacheNames = CacheConfiguration.CAMPAIGNS_CACHE)
  public List<Campaign> findAll() {
    return buildQuery(qCampaign)
      .where(qCampaign.enabled.isTrue())
      .fetch();
  }

  public Campaign findOne(long id) {
    return get(id, Campaign.class);
  }

  public Campaign findCampaignForRide(Ride ride) {
    return buildQuery(qCampaign)
      .where(qCampaign.rides.contains(ride))
      .fetchFirst();
  }

  public List<Campaign> findByProvider(long id) {
    return buildQuery(qCampaign)
      .where(
        qCampaign.provider.id.eq(id),
        qCampaign.enabled.isTrue()
      )
      .fetch();
  }

  public List<CampaignRiderDto> listSubscribedRiders(long id) {
    final List ridersRaw = buildQuery(qCampaign)
      .where(qCampaign.id.eq(id))
      .select(qCampaign.subscribedRiders)
      .fetch();
    final List<CampaignRider> riders = (List<CampaignRider>) ridersRaw;
    return riders.stream()
      .filter(CampaignRider::isEnabled)
      .map(CampaignRider::getRider)
      .map(e -> new CampaignRiderDto(e.getFirstname(), e.getLastname(), e.getEmail()))
      .collect(Collectors.toList());
  }

}
