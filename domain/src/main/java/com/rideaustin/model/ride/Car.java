package com.rideaustin.model.ride;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PostLoad;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Type;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.rideaustin.model.BaseEntity;
import com.rideaustin.model.enums.CarInspectionStatus;
import com.rideaustin.model.user.Driver;
import com.rideaustin.service.user.CarTypesUtils;

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
@Table(name = "cars")
@JsonIgnoreProperties(ignoreUnknown = true)
public class Car extends BaseEntity {

  @Column(nullable = false)
  private String color;

  @Column(nullable = false)
  private String license;

  @Column(nullable = false)
  private String make;

  @Column(nullable = false)
  private String model;

  @Column(nullable = false)
  private String year;

  @Column
  private boolean selected;

  @Column(name = "inspection_status")
  @Enumerated(EnumType.STRING)
  private CarInspectionStatus inspectionStatus = CarInspectionStatus.NOT_INSPECTED;

  @Column(name = "inspection_notes")
  @Type(type = "text")
  private String inspectionNotes;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "driver_id")
  @JsonIgnore
  private Driver driver;

  @Column(name = "removed")
  private boolean removed = false;

  @Column(name = "car_categories_bitmask")
  @JsonIgnore
  private int carCategoriesBitmask = 1;

  @Transient
  private Set<String> carCategories = new HashSet<>();

  @Transient
  private boolean inspectionSticker;

  @PostLoad
  private void onLoad() {
    carCategories = new HashSet<>();
    carCategories.addAll(CarTypesUtils.fromBitMask(carCategoriesBitmask));
  }

  public void updateByAdmin(Car car) {
    setColor(car.getColor());
    setLicense(car.getLicense());
    setMake(car.getMake());
    setModel(car.getModel());
    setYear(car.getYear());
    setCarCategories(car.getCarCategories());
    setCarCategoriesBitmask(CarTypesUtils.toBitMask(car.getCarCategories()));
    setInspectionStatus(car.getInspectionStatus());
    setInspectionNotes(car.getInspectionNotes());
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


