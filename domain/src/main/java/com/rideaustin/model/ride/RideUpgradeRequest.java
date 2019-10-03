package com.rideaustin.model.ride;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import com.rideaustin.Constants;
import com.rideaustin.model.BaseEntity;
import com.rideaustin.model.enums.RideUpgradeRequestStatus;

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
@Table(name = "ride_upgrade_requests")
public class RideUpgradeRequest extends BaseEntity {

  @Column(name = "requested_by")
  private long requestedBy;

  @Column(name = "requested_from")
  private long requestedFrom;

  @Column(name = "ride_id")
  private long rideId;

  @Column(name = "status")
  @Enumerated(EnumType.STRING)
  private RideUpgradeRequestStatus status;

  @Column(name = "source", nullable = false)
  private String source;

  @Column(name = "target", nullable = false)
  private String target;

  @Column(name = "expires_on")
  @Temporal(TemporalType.TIMESTAMP)
  private Date expiresOn;

  @Column(name = "surge_factor")
  private BigDecimal surgeFactor = Constants.NEUTRAL_SURGE_FACTOR;

  public RideUpgradeRequest(RideUpgradeRequestStatus status, String source, String target, BigDecimal surgeFactor) {
    this.status = status;
    this.source = source;
    this.target = target;
    this.surgeFactor = surgeFactor;
  }

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
