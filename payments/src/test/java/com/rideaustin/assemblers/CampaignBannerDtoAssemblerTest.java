package com.rideaustin.assemblers;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.rideaustin.model.Campaign;
import com.rideaustin.rest.model.CampaignBannerDto;

public class CampaignBannerDtoAssemblerTest {

  private CampaignBannerDtoAssembler testedInstance;

  @Before
  public void setUp() throws Exception {
    testedInstance = new CampaignBannerDtoAssembler();
  }

  @Test
  public void toDtoSkipsNull() {
    final CampaignBannerDto result = testedInstance.toDto((Campaign) null);

    assertNull(result);
  }

  @Test
  public void toDtoCopiesData() {
    final Campaign campaign = new Campaign();
    final long id = 1L;
    final String description = "Description";
    final String bannerIcon = "icon";
    final boolean showMap = true;
    final boolean showDetails = true;
    campaign.setId(id);
    campaign.setDescription(description);
    campaign.setBannerIcon(bannerIcon);
    campaign.setShowMap(showMap);
    campaign.setShowDetails(showDetails);
    final CampaignBannerDto result = testedInstance.toDto(campaign);

    assertEquals(id, result.getId());
    assertEquals(description, result.getBannerText());
    assertEquals(bannerIcon, result.getBannerIcon());
    assertEquals(showMap, result.isShouldShowMap());
    assertEquals(showDetails, result.isShouldShowDetail());
  }
}