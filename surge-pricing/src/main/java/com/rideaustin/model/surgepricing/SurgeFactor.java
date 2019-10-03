package com.rideaustin.model.surgepricing;

import static com.rideaustin.Constants.MAXIMUM_SURGE_FACTOR_STR;
import static com.rideaustin.Constants.NEUTRAL_SURGE_FACTOR_STR;

import java.math.BigDecimal;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;

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
@Table(name = "surge_factors")
public class SurgeFactor extends BaseEntity implements Comparable<SurgeFactor> {

  @ManyToOne
  @JoinColumn(name = "surge_area_id")
  private SurgeArea surgeArea;

  @Column(name = "car_type")
  private String carType;

  @Column(name = "value", nullable = false)
  @NotNull
  @DecimalMin(NEUTRAL_SURGE_FACTOR_STR)
  @DecimalMax(MAXIMUM_SURGE_FACTOR_STR)
  private BigDecimal value;

  @Override
  public int compareTo(SurgeFactor other) {
    return this.value.compareTo(other.getValue());
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
    return Objects.hash(super.hashCode(), surgeArea, carType, value);
  }
}
