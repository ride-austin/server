package com.rideaustin.model.ride;

import java.util.Date;
import java.util.Optional;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.hibernate.annotations.DynamicUpdate;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.rideaustin.model.BaseEntity;
import com.rideaustin.model.LocationAware;
import com.rideaustin.model.enums.ActiveDriverStatus;
import com.rideaustin.model.user.Driver;
import com.rideaustin.service.location.ObjectLocationUtil;
import com.rideaustin.service.location.enums.LocationType;
import com.rideaustin.service.location.model.LocationObject;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@Builder
@DynamicUpdate
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "active_drivers")
@JsonIgnoreProperties(ignoreUnknown = true)
public class ActiveDriver extends BaseEntity implements LocationAware {

  @Column
  @Enumerated(EnumType.STRING)
  private ActiveDriverStatus status;

  @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.REFRESH})
  @JoinColumn(name = "driver_id")
  private Driver driver;

  @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.REFRESH})
  @JoinColumn(name = "car_id")
  private Car selectedCar;

  @Column(name = "inactive_on")
  @Temporal(TemporalType.TIMESTAMP)
  private Date inactiveOn;

  @Column(name = "city_id")
  private Long cityId;

  @Transient
  private LocationObject locationObject;

  public void setLocationObject(LocationObject locationObject) {
    this.locationObject = locationObject;
  }

  @JsonProperty("latitude")
  public double getLatitude() {
    return Optional.ofNullable(updateLocationObject()).map(LocationObject::getLatitude).orElse(0d);
  }

  @JsonProperty("longitude")
  public double getLongitude() {
    return Optional.ofNullable(updateLocationObject()).map(LocationObject::getLongitude).orElse(0d);
  }

  @JsonProperty("speed")
  public double getSpeed() {
    return Optional.ofNullable(updateLocationObject()).map(LocationObject::getSpeed).orElse(0d);
  }

  @JsonProperty("heading")
  public Double getHeading() {
    return Optional.ofNullable(updateLocationObject()).map(LocationObject::getHeading).orElse(0d);
  }

  @JsonProperty("course")
  public Double getCourse() {
    return Optional.ofNullable(updateLocationObject()).map(LocationObject::getCourse).orElse(0d);
  }

  private LocationObject updateLocationObject() {
    locationObject = Optional.ofNullable(locationObject).orElse(ObjectLocationUtil.get(getId(), LocationType.ACTIVE_DRIVER));
    return locationObject;
  }

  @JsonProperty("locationUpdatedOn")
  public Date getLocationUpdatedOn() {
    return Optional.ofNullable(updateLocationObject()).map(LocationObject::getLocationUpdateDate).orElse(new Date());
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
