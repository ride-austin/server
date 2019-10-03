package com.rideaustin.model.surgepricing;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.PostLoad;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotEmpty;

import com.rideaustin.Constants;
import com.rideaustin.model.BaseEntity;
import com.rideaustin.model.ride.CarType;
import com.rideaustin.utils.SurgeUtils;

import lombok.AccessLevel;
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
@Table(name = "surge_areas")
public class SurgeArea extends BaseEntity {

  @Column(name = "name", nullable = false, unique = true)
  @NotEmpty
  private String name;

  @Column(name = "car_categories_bitmask")
  private int carCategoriesBitmask = 7;

  @Valid
  @NotNull
  @JoinColumn(name = "area_geometry_id")
  @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
  private AreaGeometry areaGeometry;

  @Column(name = "is_active")
  private boolean active = true;

  @Column(name = "automated")
  private boolean automated = false;

  @Column(name = "city_id", nullable = false)
  @NotNull
  private Long cityId;

  @OneToMany(orphanRemoval = true, cascade = CascadeType.ALL, fetch = FetchType.EAGER)
  @JoinColumn(name = "surge_area_id")
  private Set<SurgeFactor> surgeFactors = new HashSet<>();

  @Transient
  @Setter(AccessLevel.PRIVATE)
  private Map<String, BigDecimal> surgeMapping = new HashMap<>();

  @PostLoad
  private void onLoad() {
    surgeMapping = SurgeUtils.createSurgeMapping(getSurgeFactors(), carCategoriesBitmask);
  }

  public BigDecimal getSurgeFactor(CarType carType) {
    return surgeMapping.getOrDefault(carType.getCarCategory(), Constants.NEUTRAL_SURGE_FACTOR);
  }

  public BigDecimal getSurgeFactor(String carType) {
    return surgeMapping.get(carType);
  }

  public void updateSurgeFactors(Map<String, BigDecimal> surgeMapping) {
    for (Map.Entry<String, BigDecimal> entry : surgeMapping.entrySet()) {
      Optional<SurgeFactor> surgeFactor = SurgeUtils.findSurgeFactor(getSurgeFactors(), entry.getKey());
      if (surgeFactor.isPresent()) {
        surgeFactor.get().setValue(entry.getValue());
      } else {
        surgeFactors.add(new SurgeFactor(this, entry.getKey(), entry.getValue()));
      }
    }
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
