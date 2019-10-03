package com.rideaustin.assemblers;

import org.springframework.stereotype.Component;

import com.rideaustin.model.Campaign;
import com.rideaustin.rest.model.CampaignBannerDto;

@Component
public class CampaignBannerDtoAssembler implements SingleSideAssembler<Campaign, CampaignBannerDto> {
  @Override
  public CampaignBannerDto toDto(Campaign campaign) {
    if (campaign == null) {
      return null;
    }
    return new CampaignBannerDto(campaign.getId(), campaign.getDescription(), campaign.getBannerIcon(),
      campaign.isShowMap(), campaign.isShowDetails());
  }
}
