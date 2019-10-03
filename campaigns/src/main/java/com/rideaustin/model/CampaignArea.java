package com.rideaustin.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import com.google.maps.model.LatLng;
import com.rideaustin.model.enums.CampaignAreaType;
import com.rideaustin.model.surgepricing.AreaGeometry;
import com.rideaustin.utils.GeometryUtils;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "campaign_areas")
public class CampaignArea {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", unique = true, nullable = false)
  private long id;

  @OneToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "area_id")
  private AreaGeometry area;

  @Column(name = "\"type\"")
  @Enumerated(EnumType.STRING)
  private CampaignAreaType type;

  @Column(name = "subtype")
  @Enumerated(EnumType.STRING)
  private CampaignAreaType.SubType subType;

  public boolean contains(LatLng latLng) {
    GeometryUtils.updatePolygon(area);
    return area.getPolygon().contains(latLng.lat, latLng.lng);
  }

  public boolean contains(double lat, double lng) {
    return contains(new LatLng(lat, lng));
  }
}
