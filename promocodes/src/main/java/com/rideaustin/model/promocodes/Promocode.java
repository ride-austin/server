package com.rideaustin.model.promocodes;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import com.rideaustin.model.BaseEntity;
import com.rideaustin.model.user.Rider;
import com.rideaustin.utils.SafeZeroUtils;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "promocodes")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Promocode extends BaseEntity {

  @Column(name = "promocode_type", nullable = false)
  @Enumerated(EnumType.STRING)
  private PromocodeType promocodeType = PromocodeType.PUBLIC;

  @Column(name = "title", length = 50)
  private String title;

  @Column(name = "code_literal", nullable = false, unique = true)
  private String codeLiteral;

  @Column(name = "code_value", nullable = false)
  private BigDecimal codeValue;

  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "starts_on")
  private Date startsOn;

  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "ends_on")
  private Date endsOn;

  @Column(name = "new_riders_only", nullable = false)
  private boolean newRidersOnly = true;

  @Column(name = "maximum_redemption")
  private Long maximumRedemption;

  @Column(name = "maximum_uses_per_account")
  private Integer maximumUsesPerAccount;

  @Column(name = "current_redemption")
  private Long currentRedemption = 0L;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "owner_id")
  private Rider owner;

  @Column(name = "driver_id")
  private Long driverId;

  @Column(name = "city_bitmask")
  private Integer cityBitmask;

  @Column(name = "car_type_bitmask")
  private Integer carTypeBitmask;

  @Column(name = "valid_for_number_of_rides")
  private Integer validForNumberOfRides = 1;

  @Column(name = "valid_for_number_of_days")
  private Integer validForNumberOfDays;

  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "use_end_date")
  private Date useEndDate;

  @Column(name = "next_trip_only", nullable = false)
  private boolean nextTripOnly = false;

  @Column(name = "applicable_to_fees", nullable = false)
  private boolean applicableToFees = false;

  @Column(name = "capped_amount_per_use")
  private BigDecimal cappedAmountPerUse;

  public void increaseCurrentRedemption() {
    this.currentRedemption = SafeZeroUtils.safeZero(this.currentRedemption) + 1;
  }

  public BigDecimal getMaximumPromotionValue() {
    if (maximumRedemption != null && codeValue != null) {
      return codeValue.multiply(BigDecimal.valueOf(maximumRedemption));
    }
    return null;
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
