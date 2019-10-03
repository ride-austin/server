package com.rideaustin.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import com.rideaustin.model.user.Rider;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@NoArgsConstructor
@Table(name = "campaign_riders")
public class CampaignRider {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", unique = true, nullable = false)
  private long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "campaign_id")
  private Campaign campaign;

  @OneToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "rider_id")
  private Rider rider;

  @Column(name = "enabled")
  private boolean enabled;

  public CampaignRider(Rider rider, Campaign campaign) {
    this.rider = rider;
    this.campaign = campaign;
    this.enabled = true;
  }
}
