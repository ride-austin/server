package com.rideaustin.model;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.rideaustin.model.surgepricing.AreaGeometry;

import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "area")
@JsonIgnoreProperties(ignoreUnknown = true)
public class Area extends BaseEntity {

  @Column(name = "name")
  private String name;

  @Column(name = "key_name")
  private String key;

  @Column(name = "description")
  private String description;

  @Column(name = "icon_url")
  private String iconUrl;

  @Column(name = "enabled")
  private boolean enabled;

  @Column(name = "city_id")
  private Long cityId;

  @Column(name = "visible_to_drivers")
  private boolean visibleToDrivers;

  @Column(name = "parent_area_id")
  private Long parentAreaId;

  @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
  @JoinColumn(name = "area_geometry_id")
  private AreaGeometry areaGeometry;

  @OneToMany(fetch = FetchType.EAGER, mappedBy = "containingArea")
  private Set<AreaExclusion> exclusions = new HashSet<>();

  @Column(name = "map_icon_url")
  private String mapIconUrl;

  @Column(name = "map_icon_coords")
  private String mapIconCoords;

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

