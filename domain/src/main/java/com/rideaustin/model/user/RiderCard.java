package com.rideaustin.model.user;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.joda.time.Days;
import org.joda.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.rideaustin.model.BaseEntity;
import com.rideaustin.model.enums.CardBrand;

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
@Table(name = "rider_cards")
@JsonIgnoreProperties(ignoreUnknown = true)
public class RiderCard extends BaseEntity {

  public static final int FAILED_CHARGE_THRESHOLD = 8;

  @JsonIgnore
  @Column(name = "stripe_card_id", nullable = false)
  private String stripeCardId;

  /**
   * The last 4 digits of a credit/debit card
   */
  @Column(name = "card_number", nullable = false, length = 4)
  private String cardNumber;

  @Column(name = "card_brand", nullable = false)
  @Enumerated(EnumType.STRING)
  private CardBrand cardBrand = CardBrand.UNKNOWN;

  @Column(name = "card_expired", nullable = false)
  private boolean cardExpired = false;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "rider_id")
  @JsonIgnore
  private Rider rider;

  @Column(name = "fingerprint")
  private String fingerprint;

  @Transient
  private boolean primary;

  @Column(name = "removed")
  private boolean removed = false;

  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "sync_date", nullable = false)
  private Date syncDate;

  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "last_failure_date")
  private Date lastFailureDate;

  @Column(name = "failed_charge_attempts")
  private int failedChargeAttempts;

  @Column(name = "expiration_month", length = 2)
  private String expirationMonth;

  @Column(name = "expiration_year", length = 4)
  private String expirationYear;

  @Override
  @PrePersist
  protected void onCreate() {
    super.onCreate();
    this.syncDate = new Date();
  }

  public boolean isChargeable() {
    return !isFailedChargeThresholdExceeded() && (lastFailureDate == null || Days.daysBetween(new LocalDate(lastFailureDate), new LocalDate()).isGreaterThan(Days.ONE));
  }

  public boolean isFailedChargeThresholdExceeded() {
    return failedChargeAttempts >= FAILED_CHARGE_THRESHOLD;
  }

  public void increaseFailedAttempts() {
    failedChargeAttempts++;
  }

  public void resetFailureCount() {
    failedChargeAttempts = 0;
    lastFailureDate = null;
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


