package com.rideaustin.model.user;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.rideaustin.model.BaseEntity;
import com.rideaustin.model.ride.Ride;

import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "rider_card_locks")
@JsonIgnoreProperties(ignoreUnknown = true)
public class RiderCardLock extends BaseEntity {

  @Column(name = "card_fingerprint", nullable = false)
  private String cardFingerprint;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "ride_id")
  private Ride ride;

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
