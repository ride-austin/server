package com.rideaustin.test.response;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.rideaustin.model.Area;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class AreaQueuePositions {

  private String areaQueueName;
  private String iconUrl;

  private Map<String, Integer> positions = new HashMap<>();
  private Map<String, Integer> lengths = new HashMap<>();

  public AreaQueuePositions() {}

  public AreaQueuePositions(Area area, Map<String, Integer> lengths) {
    this.areaQueueName = area.getName();
    this.iconUrl = area.getIconUrl();
    this.lengths = lengths;
  }
}
