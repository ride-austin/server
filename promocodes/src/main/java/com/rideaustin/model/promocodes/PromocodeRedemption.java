package com.rideaustin.model.promocodes;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import com.rideaustin.model.BaseEntity;
import com.rideaustin.model.user.Rider;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "promocode_redemptions")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PromocodeRedemption extends BaseEntity {

  @ManyToOne
  @JoinColumn(name = "promocode_id")
  private Promocode promocode;

  @ManyToOne
  @JoinColumn(name = "rider_id")
  private Rider rider;

  @Column(name = "applied_to_owner")
  private boolean appliedToOwner;

  @Column(name = "active")
  private boolean active = true;

  @Column(name = "original_value")
  private BigDecimal originalValue;

  @Column(name = "remaining_value")
  private BigDecimal remainingValue;

  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "valid_until")
  private Date validUntil;

  @Column(name = "number_of_times_used")
  private Integer numberOfTimesUsed = 0;

  public void decreaseRemainingValue(BigDecimal amount) {
    this.remainingValue = remainingValue.subtract(amount);
  }

  public void increaseNumberOfTimesUsed() {
    this.numberOfTimesUsed += 1;
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
