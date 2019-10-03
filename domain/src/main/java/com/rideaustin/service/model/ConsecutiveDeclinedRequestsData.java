package com.rideaustin.service.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class ConsecutiveDeclinedRequestsData extends HashMap<String, Integer> {

  public ConsecutiveDeclinedRequestsData(Collection<String> carCategories) {
    for (String carCategory : carCategories) {
      put(carCategory, 0);
    }
  }

  public ConsecutiveDeclinedRequestsData(Map<String, Integer> data) {
    super(data);
  }

  public Integer increase(String key) {
    Integer newValue = tryIncrease(key);
    put(key, newValue);
    return newValue;
  }

  public Integer tryIncrease(String key) {
    return Optional.ofNullable(get(key)).orElse(0) + 1;
  }

  public void reset(String key) {
    put(key, 0);
  }

}
