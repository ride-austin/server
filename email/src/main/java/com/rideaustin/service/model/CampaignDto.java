package com.rideaustin.service.model;

import org.joda.money.Money;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class CampaignDto {

  private final String name;
  private final Money cappedAmount;
  private final String headerImage;

}
