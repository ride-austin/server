package com.rideaustin.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.rideaustin.model.enums.GeolocationLogEvent;
import com.rideaustin.model.ride.CarType;
import com.rideaustin.model.user.Rider;

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
@Table(name = "geo_log")
@JsonIgnoreProperties(ignoreUnknown = true)
public class GeolocationLog extends BaseEntity {

  @Column(name = "location_lat")
  private Double locationLat;

  @Column(name = "location_lng")
  private Double locationLng;

  @Column
  @Enumerated(EnumType.STRING)
  private GeolocationLogEvent event;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "rider_id")
  @JsonIgnore
  private Rider rider;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "car_category")
  private CarType carType;

  public Date getDate() {
    return this.getCreatedDate();
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
