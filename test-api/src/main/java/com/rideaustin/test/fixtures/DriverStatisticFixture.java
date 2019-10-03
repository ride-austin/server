package com.rideaustin.test.fixtures;

import com.rideaustin.driverstatistic.model.DriverId;
import com.rideaustin.driverstatistic.model.DriverStatistic;

public class DriverStatisticFixture extends AbstractFixture<DriverStatistic> {

  private final long driverId;
  private final int lastAccepted;
  private final int lastAcceptedOver;
  private final int lastCancelled;
  private final int lastCancelledOver;

  public DriverStatisticFixture(long driverId, int lastAccepted, int lastAcceptedOver, int lastCancelled, int lastCancelledOver) {
    this.driverId = driverId;
    this.lastAccepted = lastAccepted;
    this.lastAcceptedOver = lastAcceptedOver;
    this.lastCancelled = lastCancelled;
    this.lastCancelledOver = lastCancelledOver;
  }

  @Override
  protected DriverStatistic createObject() {
    DriverStatistic driverStatistic = new DriverStatistic(new DriverId(driverId));
    driverStatistic.setLastCancelledOver(lastCancelledOver);
    driverStatistic.setLastCancelledCount(lastCancelled);
    driverStatistic.setLastAcceptedOver(lastAcceptedOver);
    driverStatistic.setLastAcceptedCount(lastAccepted);
    return driverStatistic;
  }
}
