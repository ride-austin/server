package com.rideaustin.model.user;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.hibernate.annotations.DynamicUpdate;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.rideaustin.Constants;
import com.rideaustin.model.Charity;
import com.rideaustin.model.enums.AvatarType;
import com.rideaustin.model.enums.PaymentStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Builder
@DynamicUpdate
@Table(name = "riders")
@JsonIgnoreProperties(ignoreUnknown = true)
@AllArgsConstructor
public class Rider extends Avatar {

  @Column(name = "stripe_id")
  private String stripeId;

  @JsonIgnore
  @OneToOne
  @JoinColumn(name = "primary_card_id")
  private RiderCard primaryCard;

  @Column
  private Double rating = 5D;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "charity_id")
  private Charity charity;

  @Enumerated(EnumType.STRING)
  @Column(name = "payment_status")
  @JsonIgnore
  private PaymentStatus paymentStatus;

  @Column(name = "city_id")
  private Long cityId = Constants.DEFAULT_CITY_ID;

  @Column(name = "dispatcher_account")
  private boolean dispatcherAccount;

  public Rider() {
    super();
    setActive(true);
  }

  @Override
  public AvatarType getType() {
    return AvatarType.RIDER;
  }

  public void updateByAdmin(Rider rider) {
    super.updateByAdmin(rider);
    setCharity(rider.getCharity());
  }

  public void updateByUser(Rider rider) {
    super.updateByUser(rider);
    setCharity(rider.getCharity());
  }

  @Override
  public Long getCityId() {
    return cityId;
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

