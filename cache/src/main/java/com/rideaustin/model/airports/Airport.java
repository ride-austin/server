package com.rideaustin.model.airports;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.joda.money.Money;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.rideaustin.model.Area;
import com.rideaustin.model.BaseEntity;
import com.rideaustin.model.helper.MoneyConverter;

import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "airports")
@JsonIgnoreProperties(ignoreUnknown = true)
public class Airport extends BaseEntity {

  @Column(name = "name")
  private String name;

  @Convert(converter = MoneyConverter.class)
  @Column(name = "pickup_fee")
  private Money pickupFee;

  @Convert(converter = MoneyConverter.class)
  @Column(name = "dropoff_fee")
  private Money dropoffFee;

  @Column(name = "enabled")
  private boolean enabled;

  @Column(name = "city_id")
  private Long cityId;

  @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
  @JoinColumn(name = "area_id")
  private Area area;

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

