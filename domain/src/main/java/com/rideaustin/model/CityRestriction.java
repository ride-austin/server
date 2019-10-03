package com.rideaustin.model;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.PostLoad;
import javax.persistence.Table;

import com.rideaustin.model.surgepricing.AreaGeometry;
import com.rideaustin.utils.GeometryUtils;

import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "city_restricted_areas")
public class CityRestriction extends BaseEntity {

  @Column(name = "city_id")
  private Long cityId;

  @JoinColumn(name = "zone_id")
  @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
  private AreaGeometry areaGeometry;

  @Column
  private boolean enabled;

  @PostLoad
  private void onLoad() {
    GeometryUtils.updatePolygon(areaGeometry);
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
