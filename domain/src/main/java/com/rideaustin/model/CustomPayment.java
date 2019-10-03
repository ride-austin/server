package com.rideaustin.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.joda.money.Money;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.rideaustin.model.enums.CustomPaymentCategory;
import com.rideaustin.model.helper.MoneyConverter;
import com.rideaustin.model.user.Administrator;
import com.rideaustin.model.user.Driver;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "custom_payment")
public class CustomPayment extends BaseEntity {

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "administrator_id")
  private Administrator creator;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "driver_id")
  private Driver driver;

  @Convert(converter = MoneyConverter.class)
  @Column(name = "value")
  private Money value;

  @Column(name = "category")
  @Enumerated(EnumType.STRING)
  private CustomPaymentCategory category;

  @Column(name = "description")
  private String description;

  @Column(name = "payment_date")
  @Temporal(TemporalType.TIMESTAMP)
  @JsonIgnore
  private Date paymentDate;

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