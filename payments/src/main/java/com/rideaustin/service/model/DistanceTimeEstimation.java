package com.rideaustin.service.model;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class DistanceTimeEstimation {
  private final DistanceTime distanceTime;
  private final BigDecimal distanceInMiles;
  private final BigDecimal estimatedTime;
}
