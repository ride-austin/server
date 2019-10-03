package com.rideaustin.report.entry;

import java.math.BigDecimal;
import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SurgeHistoryEntry {

  private final Instant createdDate;
  private final BigDecimal surgeFactor;

}
