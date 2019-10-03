package com.rideaustin.model;

import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "campaign_providers")
public class CampaignProvider extends BaseEntity {

  @Column(name = "name")
  private String name;
  @Column(name = "menu_icon")
  private String menuIcon;
  @Column(name = "enabled")
  private boolean enabled;
  @Column(name = "city_id")
  private long cityId;
  @OneToMany(fetch = FetchType.EAGER, mappedBy = "provider")
  private Set<Campaign> campaigns;
  @Column(name = "shown_in_menu")
  private boolean shownInMenu;

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
