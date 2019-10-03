package com.rideaustin.model.ride;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.rideaustin.model.BaseEntity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "queued_rides")
public class RideQueueToken extends BaseEntity {

  @Column(name = "ride_id")
  private long rideId;
  @Column(name = "token")
  private String token;
  @Column(name = "expires_on")
  private Date expiresOn;
  @Column(name = "expired")
  private boolean expired;

  public RideQueueToken(long rideId, String token, Date expiresOn) {
    this.rideId = rideId;
    this.token = token;
    this.expiresOn = expiresOn;
    this.expired = false;
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
