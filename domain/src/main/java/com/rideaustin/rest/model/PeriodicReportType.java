package com.rideaustin.rest.model;

import java.time.Period;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAdjuster;

import javax.annotation.Nonnull;

public enum PeriodicReportType {

  DAILY(d -> d.minus(Period.ofDays(1)), Period.ofDays(1), ""),
  WEEKLY(d -> d.with(ChronoField.DAY_OF_WEEK, 1).minus(Period.ofWeeks(1)), Period.ofWeeks(1),
      "week starting ");

  private final TemporalAdjuster startAdjuster;
  private final Period period;
  private String periodDescription;

  PeriodicReportType(@Nonnull TemporalAdjuster startAdjuster, @Nonnull Period period,
    @Nonnull String periodDescription) {
    this.startAdjuster = startAdjuster;
    this.period = period;
    this.periodDescription = periodDescription;
  }

  @Nonnull
  public TemporalAdjuster getStartAdjuster() {
    return startAdjuster;
  }

  @Nonnull
  public Period getPeriod() {
    return period;
  }

  @Nonnull
  public String getPeriodDescription() {
    return periodDescription;
  }

}
