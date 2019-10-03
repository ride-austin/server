package com.rideaustin.driverstatistic;

import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.Assertions;

import com.rideaustin.driverstatistic.model.DriverStatistic;

public class DriverStatisticAssert extends AbstractAssert<DriverStatisticAssert, DriverStatistic> {

  private DriverStatisticAssert(DriverStatistic driverStatistic) {
    super(driverStatistic, DriverStatisticAssert.class);
  }

  public static DriverStatisticAssert assertThat(DriverStatistic driverStatistic) {
    return new DriverStatisticAssert(driverStatistic);
  }

  public DriverStatisticAssert hasAcceptedCountEqualTo(int count) {
    Assertions.assertThat(actual.getAcceptedCount()).isEqualTo(count);
    return this;
  }

  public DriverStatisticAssert hasAcceptedCountGreaterThan(int count) {
    Assertions.assertThat(actual.getAcceptedCount()).isGreaterThan(count);
    return this;
  }

  public DriverStatisticAssert hasCancelledCountEqualTo(int count) {
    Assertions.assertThat(actual.getCancelledCount()).isEqualTo(count);
    return this;
  }

  public DriverStatisticAssert hasCancelledCountGreaterThan(int count) {
    Assertions.assertThat(actual.getCancelledCount()).isGreaterThan(count);
    return this;
  }


}
