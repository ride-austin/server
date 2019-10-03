package com.rideaustin.rest.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class CampaignMenuDto {
  private final long id;
  private final String menuTitle;
  private final String menuIcon;
}
