package com.rideaustin.user.tracking.model;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserTrackData {

  private String utmSource;
  private String utmMedium;
  private String utmCampaign;
  private String promoCode;
  private String marketingTitle;

  public UserTrackData() {
  }

  public UserTrackData(Map<String, String[]> parameters) {
    this.utmSource = parameters.getOrDefault("utm_source", new String[]{null})[0];
    this.utmMedium = parameters.getOrDefault("utm_medium", new String[]{null})[0];
    this.utmCampaign = parameters.getOrDefault("utm_campaign", new String[]{null})[0];
    this.promoCode = parameters.getOrDefault("promo_code", new String[]{null})[0];
    this.marketingTitle = parameters.getOrDefault("marketing_title", new String[]{null})[0];
  }

  public boolean isEmpty() {
    return StringUtils.isEmpty(utmSource) && StringUtils.isEmpty(utmMedium) && StringUtils.isEmpty(utmCampaign) &&
      StringUtils.isEmpty(promoCode) && StringUtils.isEmpty(marketingTitle);
  }
}
