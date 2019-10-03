package com.rideaustin.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@NoArgsConstructor
@Table(name = "ride_calls")
public class RideCall extends BaseEntity {

  @Column(name = "\"from\"")
  private String from;
  @Column(name = "\"to\"")
  private String to;
  @Column(name = "ride_id")
  private Long rideId;
  @Column(name = "call_sid")
  private String callSid;
  @Column(name = "\"type\"")
  @Enumerated(EnumType.STRING)
  private RideCallType type;
  @Column(name = "processed")
  private boolean processed = false;

  public RideCall(String from, String to, Long rideId, String callSid, RideCallType type) {
    this.from = from;
    this.to = to;
    this.callSid = callSid;
    this.type = type;
    this.rideId = rideId;
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
