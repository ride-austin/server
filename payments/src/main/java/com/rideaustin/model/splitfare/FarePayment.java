package com.rideaustin.model.splitfare;

import java.util.Date;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.joda.money.Money;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.rideaustin.model.BaseEntity;
import com.rideaustin.model.enums.PaymentProvider;
import com.rideaustin.model.enums.PaymentStatus;
import com.rideaustin.model.enums.SplitFareStatus;
import com.rideaustin.model.helper.MoneyConverter;
import com.rideaustin.model.ride.Ride;
import com.rideaustin.model.user.Rider;
import com.rideaustin.model.user.RiderCard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "fare_payments")
@JsonIgnoreProperties(ignoreUnknown = true)
public class FarePayment extends BaseEntity {

  @Column(name = "split_status")
  @Enumerated(EnumType.STRING)
  private SplitFareStatus splitStatus;

  @Column(name = "payment_status")
  @Enumerated(EnumType.STRING)
  private PaymentStatus paymentStatus;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "rider_id")
  private Rider rider;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "ride_id")
  private Ride ride;

  @Column(name = "main_rider", nullable = false)
  private boolean mainRider = false;

  @Convert(converter = MoneyConverter.class)
  @Column(name = "free_credit_used")
  private Money freeCreditCharged;

  @Convert(converter = MoneyConverter.class)
  @Column(name = "stripe_credit_charged")
  private Money stripeCreditCharge;

  @JsonIgnore
  @Column(name = "charge_id")
  private String chargeId;

  @JsonIgnore
  @OneToOne
  @JoinColumn(name = "card_id")
  private RiderCard usedCard;

  @Column(name = "provider")
  @Enumerated(EnumType.STRING)
  private PaymentProvider provider = PaymentProvider.CREDIT_CARD;

  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "charge_scheduled")
  private Date chargeScheduled;

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (!(obj instanceof FarePayment)) {
      return false;
    }
    FarePayment other = (FarePayment) obj;
    return this.getId() == other.getId();
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), splitStatus, rider, ride, mainRider, freeCreditCharged, stripeCreditCharge,
      chargeId, usedCard, provider, chargeScheduled);
  }
}
