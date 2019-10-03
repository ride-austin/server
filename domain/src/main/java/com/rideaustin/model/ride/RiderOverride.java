package com.rideaustin.model.ride;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import com.rideaustin.model.BaseEntity;
import com.rideaustin.model.user.Rider;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "rider_overrides")
public class RiderOverride extends BaseEntity {

  @Column(name = "first_name")
  private String firstName;
  @Column(name = "last_name")
  private String lastName;
  @Column(name = "phone_number")
  private String phoneNumber;
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "overridden_id")
  private Rider overridden;
  @OneToOne(fetch = FetchType.LAZY)
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
