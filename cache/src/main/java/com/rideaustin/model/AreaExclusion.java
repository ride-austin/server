package com.rideaustin.model;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import com.rideaustin.model.surgepricing.AreaGeometry;
import com.rideaustin.utils.GeometryUtils;

import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "area_exclusions")
public class AreaExclusion extends BaseEntity {

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "area_id")
  private Area containingArea;

  @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
  @JoinColumn(name = "exclusion_geometry_id")
  private AreaGeometry areaGeometry;

  @Column(name = "leave_area_on_enter")
  private boolean leaveAreaOnEnter;

  public boolean contains(double lat, double lng) {
    GeometryUtils.updatePolygon(areaGeometry);
    return areaGeometry.getPolygon().contains(lat, lng);
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
