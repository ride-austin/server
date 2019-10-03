package com.rideaustin.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.rideaustin.model.ride.ActiveDriver;

import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "area_queue")
@JsonIgnoreProperties(ignoreUnknown = true)
public class AreaQueueEntry extends BaseEntity {

  @ManyToOne(fetch = FetchType.EAGER, optional = false)
  @JoinColumn(name = "area_id")
  @JsonIgnore
  private Area area;

  @ManyToOne(fetch = FetchType.EAGER, optional = false)
  @JoinColumn(name = "active_driver_id")
  @JsonIgnore
  private ActiveDriver activeDriver;

  @Column(name = "enabled")
  private boolean enabled;

  @Column(name = "car_category")
  private String carCategory;

  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "last_present_in_queue")
  private Date lastPresentInQueue;

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

