package com.rideaustin.repo.dsl;

import java.util.List;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;

import com.rideaustin.config.CacheConfiguration;
import com.rideaustin.model.QCampaignProvider;
import com.rideaustin.model.rest.CampaignProviderDto;
import com.rideaustin.model.rest.QCampaignProviderDto;

@Repository
public class CampaignProviderDslRepository extends AbstractDslRepository {

  private static final QCampaignProvider qCampaignProvider = QCampaignProvider.campaignProvider;

  @Cacheable(cacheNames = CacheConfiguration.CAMPAIGN_PROVIDERS_CACHE)
  public List<CampaignProviderDto> getAll(long cityId) {
    return buildQuery(qCampaignProvider)
      .select(new QCampaignProviderDto(qCampaignProvider.id, qCampaignProvider.name, qCampaignProvider.menuIcon))
      .where(
        qCampaignProvider.enabled.isTrue(),
        qCampaignProvider.cityId.eq(cityId),
        qCampaignProvider.shownInMenu.isTrue()
        )
      .fetch();
  }
}
