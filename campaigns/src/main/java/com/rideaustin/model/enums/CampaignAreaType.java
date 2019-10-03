package com.rideaustin.model.enums;

import lombok.Getter;

public enum CampaignAreaType {

  PICKUP,
  DROPOFF
  ;

  public enum SubType {
    AREA("#6897B8"),
    BUS_STOP("#78AC77")
    ;

    @Getter
    private final String color;

    SubType(String color) {
      this.color = color;
    }
  }
}
