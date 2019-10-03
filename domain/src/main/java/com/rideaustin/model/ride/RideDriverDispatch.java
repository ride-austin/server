package com.rideaustin.model.ride;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.UniqueConstraint;

import com.rideaustin.model.BaseEntity;
import com.rideaustin.model.enums.DispatchStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "ride_driver_dispatches", uniqueConstraints = @UniqueConstraint(columnNames = {"ride_id", "active_driver_id"}))
public class RideDriverDispatch extends BaseEntity {
  @Column(name = "dispatched_on")
  @Temporal(TemporalType.TIMESTAMP)
  private Date dispatchedOn;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "ride_id")
  private Ride ride;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "active_driver_id")
  private ActiveDriver activeDriver;

  @Enumerated(EnumType.STRING)
  @Column
  private DispatchStatus status;

  @Column(name = "driving_time_to_rider")
  private Long drivingTimeToRider;

  @Column(name = "driving_distance_to_rider")
  private Long drivingDistanceToRider;

  @Column(name = "dispatch_location_lat")
  private Double dispatchLocationLat;

  @Column(name = "dispatch_location_long")
  private Double dispatchLocationLong;

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    return super.equals(o);
  }

  @Override
  public int hashCode() {
    return super.hashCode();
  }
}
