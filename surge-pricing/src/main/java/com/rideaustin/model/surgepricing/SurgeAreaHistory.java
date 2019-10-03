package com.rideaustin.model.surgepricing;

import java.math.BigDecimal;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.rideaustin.model.BaseEntity;

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
@Table(name = "surge_areas_history")
public class SurgeAreaHistory extends BaseEntity {

  @Column(name = "surge_factor", nullable = false)
  private BigDecimal surgeFactor;

  @Column(name = "surge_factor_car_category")
  private String surgeFactorCarCategory;

  @Column(name = "recommended_surge_factor", nullable = false)
  private BigDecimal recommendedSurgeFactor;

  @Column(name = "number_of_requested_rides", nullable = false)
  private Integer numberOfRequestedRides = 0;

  @Column(name = "number_of_completed_rides", nullable = false)
  private Integer numberOfAcceptedRides = 0;

  @Column(name = "number_of_eyeballs", nullable = false)
  private Integer numberOfEyeballs = 0;

  @Column(name = "number_of_cars", nullable = false)
  private Integer numberOfCars = 0;

  @Column(name = "number_of_available_cars", nullable = false)
  private Integer numberOfAvailableCars = 0;

  @Column(name = "automated", nullable = false)
  private Boolean automated = Boolean.FALSE;

  @Column(name = "car_categories_bitmask")
  @JsonIgnore
  private int carCategoriesBitmask = 7;

  @Column(name = "name", nullable = false)
  private String name;

  @Column(name = "top_left_corner_location_lat")
  private Double topLeftCornerLat;

  @Column(name = "top_left_corner_location_lng")
  private Double topLeftCornerLng;

  @Column(name = "bottom_right_corner_location_lat")
  private Double bottomRightCornerLat;

  @Column(name = "bottom_right_corner_location_lng")
  private Double bottomRightCornerLng;

  @Column(name = "center_point_lat")
  private Double centerPointLat;

  @Column(name = "center_point_lng")
  private Double centerPointLng;

  @Column(name = "csv_geometry")
  private String csvGeometry;

  @Column(name = "surge_area_id")
  private Long surgeAreaId;

  public boolean equalsTo(SurgeAreaHistory other) {
    if (this == other) {
      return true;
    }
    return carCategoriesBitmask == other.carCategoriesBitmask &&
      Objects.equals(surgeFactor, other.surgeFactor) &&
      Objects.equals(surgeFactorCarCategory, other.surgeFactorCarCategory) &&
      recommendedSurgeFactor.compareTo(other.recommendedSurgeFactor) == 0 &&
      Objects.equals(numberOfRequestedRides, other.numberOfRequestedRides) &&
      Objects.equals(numberOfAcceptedRides, other.numberOfAcceptedRides) &&
      Objects.equals(numberOfEyeballs, other.numberOfEyeballs) &&
      Objects.equals(numberOfCars, other.numberOfCars) &&
      Objects.equals(numberOfAvailableCars, other.numberOfAvailableCars) &&
      Objects.equals(automated, other.automated) &&
      Objects.equals(name, other.name) &&
      Objects.equals(topLeftCornerLat, other.topLeftCornerLat) &&
      Objects.equals(topLeftCornerLng, other.topLeftCornerLng) &&
      Objects.equals(bottomRightCornerLat, other.bottomRightCornerLat) &&
      Objects.equals(bottomRightCornerLng, other.bottomRightCornerLng) &&
      Objects.equals(centerPointLat, other.centerPointLat) &&
      Objects.equals(centerPointLng, other.centerPointLng) &&
      Objects.equals(csvGeometry, other.csvGeometry);
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