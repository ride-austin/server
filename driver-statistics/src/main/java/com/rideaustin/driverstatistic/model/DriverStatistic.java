package com.rideaustin.driverstatistic.model;

import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import com.rideaustin.model.BaseEntity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "driver_statistics")
@NoArgsConstructor
@Getter
public class DriverStatistic extends BaseEntity {

  @NotNull
  @Embedded
  private DriverId driverId;

  @Column(name = "dispatch_count", nullable = false)
  private int dispatchCount;

  @Column(name = "accepted_count", nullable = false)
  private int acceptedCount;

  @Column(name = "last_accepted_count", nullable = false)
  @Setter
  private int lastAcceptedCount;

  @Column(name = "last_accepted_over", nullable = false)
  @Setter
  private int lastAcceptedOver;

  @Column(name = "cancelled_count", nullable = false)
  private int cancelledCount;

  @Column(name = "last_cancelled_count", nullable = false)
  @Setter
  private int lastCancelledCount;

  @Column(name = "last_cancelled_over", nullable = false)
  @Setter
  private int lastCancelledOver;

  @Column(name = "completed_count", nullable = false)
  private int completedCount;

  @Column(name = "declined_count", nullable = false)
  private int declinedCount;

  @Column(name = "received_count", nullable = false)
  private int receivedCount;

  public DriverStatistic(DriverId driverId) {
    Objects.requireNonNull(driverId);
    this.driverId = driverId;
  }

  private static void assertDependencies() {
    Objects.requireNonNull(DriverStatisticDependencies.repository, " repository dependency is null, check if Spring context is initialized properly");
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    DriverStatistic that = (DriverStatistic) o;

    return getId() == that.getId();
  }

  @Override
  public int hashCode() {
    return (int) (getId() ^ (getId() >>> 32));
  }

  public static DriverStatistic findOrCreate(DriverId driverId) {
    assertDependencies();

    DriverStatistic existing = DriverStatisticDependencies.repository.findByDriverId(driverId);

    if (existing != null) {
      return existing;
    }
    DriverStatistic statistic = new DriverStatistic(driverId);
    return DriverStatisticDependencies.repository.save(statistic);
  }

}
