package com.rideaustin.model.surgepricing;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

import com.rideaustin.model.BaseEntity;
import com.sromku.polygon.Polygon;

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
@Table(name = "area_geometries")
public class AreaGeometry extends BaseEntity {

  @Column(name = "name")
  private String name;

  @Column(name = "top_left_corner_location_lat")
  @NotNull
  private Double topLeftCornerLat;

  @Column(name = "top_left_corner_location_lng")
  @NotNull
  private Double topLeftCornerLng;

  @Column(name = "bottom_right_corner_location_lat")
  @NotNull
  private Double bottomRightCornerLat;

  @Column(name = "bottom_right_corner_location_lng")
  @NotNull
  private Double bottomRightCornerLng;

  @Column(name = "center_point_lat")
  @NotNull
  private Double centerPointLat;

  @Column(name = "center_point_lng")
  @NotNull
  private Double centerPointLng;

  @Column(name = "csv_geometry")
  @NotNull
  private String csvGeometry;

  @Column(name = "label_lat")
  @NotNull
  private Double labelLat;

  @Column(name = "label_lng")
  @NotNull
  private Double labelLng;

  @Transient
  private Polygon polygon;

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
