package com.rideaustin.report;

import static java.time.temporal.TemporalAdjusters.firstDayOfMonth;
import static java.time.temporal.TemporalAdjusters.lastDayOfMonth;

import java.time.Instant;
import java.time.LocalDate;

import com.rideaustin.Constants;

public enum DefaultDateValues {
  CURRENT_DATE {
    public Instant getValue() {
      return LocalDate.now().atStartOfDay().atZone(Constants.CST_ZONE).toInstant();
    }
  },
  START_MONTH {
    public Instant getValue() {
      return LocalDate.now().atStartOfDay().with(firstDayOfMonth()).atZone(Constants.CST_ZONE).toInstant();
    }
  },
  END_MONTH {
    public Instant getValue() {
      return LocalDate.now().atStartOfDay().with(lastDayOfMonth()).atZone(Constants.CST_ZONE).toInstant();
    }
  },
  ;

  public abstract Instant getValue();
}
