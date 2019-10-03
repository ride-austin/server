package com.rideaustin.assemblers;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.rideaustin.model.Campaign;
import com.rideaustin.model.CampaignArea;
import com.rideaustin.rest.model.CampaignDto;
import com.rideaustin.rest.model.CampaignDto.CampaignAreaDto;
import com.rideaustin.utils.GeometryUtils;

@Component
public class CampaignDtoAssembler implements SingleSideAssembler<Campaign, CampaignDto> {

  @Override
  public CampaignDto toDto(Campaign campaign) {
    if (campaign == null) {
      return null;
    }
    Map<Long, CampaignAreaDto> areas = new HashMap<>();
    for (CampaignArea area : campaign.getPickupZones()) {
      areas.put(area.getArea().getId(), new CampaignAreaDto(area.getArea().getName(), area.getSubType().getColor(),
        GeometryUtils.buildCoordinates(area.getArea().getCsvGeometry())));
    }
    for (CampaignArea area : campaign.getDropoffZones()) {
      areas.put(area.getArea().getId(), new CampaignAreaDto(area.getArea().getName(), area.getSubType().getColor(),
        GeometryUtils.buildCoordinates(area.getArea().getCsvGeometry())));
    }
    return new CampaignDto(campaign.getHeaderIcon(), campaign.getName(), campaign.getDescriptionBody(),
      campaign.getFooterText(), new HashSet<>(areas.values()));
  }

}
