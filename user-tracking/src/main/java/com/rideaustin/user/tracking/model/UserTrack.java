package com.rideaustin.user.tracking.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.rideaustin.model.BaseEntity;
import com.rideaustin.model.user.User;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@NoArgsConstructor
@Table(name = "user_tracks")
public class UserTrack extends BaseEntity {

  @Column(name = "user_id", nullable = false)
  private long userId;
  @Column(name = "utm_source")
  private String utmSource;
  @Column(name = "utm_medium")
  private String utmMedium;
  @Column(name = "utm_campaign")
  private String utmCampaign;
  @Column(name = "promo_code")
  private String promoCode;
  @Column(name = "marketing_title")
  private String marketingTitle;

  public UserTrack(User user, UserTrackData userTrackData) {
    this.userId = user.getId();
    this.utmCampaign = userTrackData.getUtmCampaign();
    this.utmMedium = userTrackData.getUtmMedium();
    this.utmSource = userTrackData.getUtmSource();
    this.promoCode = userTrackData.getPromoCode();
    this.marketingTitle = userTrackData.getMarketingTitle();
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
