package com.rideaustin.model.user;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.rideaustin.model.BaseEntity;
import com.rideaustin.model.helper.CommentConverter;
import com.rideaustin.model.ride.Ride;

import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "rating_updates")
@JsonIgnoreProperties(ignoreUnknown = true)
public class RatingUpdate extends BaseEntity {

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "rated_avatar_id")
  private Avatar ratedAvatar;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "rated_by_avatar_id")
  private Avatar ratedByAvatar;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "ride_id")
  private Ride ride;

  @Column
  private Double rating = 0D;

  @Column(name = "comment", length = 2000)
  @Convert(converter = CommentConverter.class)
  private String comment;

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
