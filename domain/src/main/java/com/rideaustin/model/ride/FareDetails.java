package com.rideaustin.model.ride;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Embeddable;

import org.joda.money.Money;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.rideaustin.Constants;
import com.rideaustin.model.helper.MoneyConverter;
import com.rideaustin.utils.SafeZeroUtils;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Builder
@ToString
@Embeddable
@NoArgsConstructor
@EqualsAndHashCode
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class FareDetails {

  @Convert(converter = MoneyConverter.class)
  @Column(name = "minimum_fare")
  private Money minimumFare;

  @Convert(converter = MoneyConverter.class)
  @Column(name = "base_fare")
  private Money baseFare;

  @Convert(converter = MoneyConverter.class)
  @Column(name = "rate_per_mile")
  private Money ratePerMile;

  @Convert(converter = MoneyConverter.class)
  @Column(name = "rate_per_minute")
  private Money ratePerMinute;

  @Convert(converter = MoneyConverter.class)
  @Column(name = "estimated_fare")
  private Money estimatedFare;

  @Convert(converter = MoneyConverter.class)
  @Column(name = "booking_fee")
  private Money bookingFee;

  @Convert(converter = MoneyConverter.class)
  @Column(name = "distance_fare")
  private Money distanceFare;

  @Convert(converter = MoneyConverter.class)
  @Column(name = "time_fare")
  private Money timeFare;

  @Convert(converter = MoneyConverter.class)
  @Column(name = "city_fee")
  private Money cityFee;

  @Convert(converter = MoneyConverter.class)
  @Column(name = "cancellation_fee")
  private Money cancellationFee;

  @Convert(converter = MoneyConverter.class)
  @Column(name = "processing_fee")
  private Money processingFee;

  @Convert(converter = MoneyConverter.class)
  @Column(name = "sub_total")
  private Money subTotal;

  @Convert(converter = MoneyConverter.class)
  @Column(name = "normal_fare")
  private Money normalFare;

  @Convert(converter = MoneyConverter.class)
  @Column(name = "surge_fare")
  private Money surgeFare;

  @Convert(converter = MoneyConverter.class)
  @Column(name = "total_fare")
  private Money totalFare;

  @Convert(converter = MoneyConverter.class)
  @Column(name = "free_credit_used")
  private Money freeCreditCharged;

  @Convert(converter = MoneyConverter.class)
  @Column(name = "stripe_credit_charged")
  private Money stripeCreditCharge;

  @Column(name = "driver_payment")
  @Convert(converter = MoneyConverter.class)
  private Money driverPayment;

  @Column(name = "ra_payment")
  @Convert(converter = MoneyConverter.class)
  private Money raPayment = Constants.ZERO_USD;

  @Column(name = "tip")
  @Convert(converter = MoneyConverter.class)
  private Money tip;

  @Convert(converter = MoneyConverter.class)
  @Column(name = "round_up_amount")
  private Money roundUpAmount;

  @Convert(converter = MoneyConverter.class)
  @Column(name = "airport_fee")
  private Money airportFee;

  public Money getTotalCharge() {
    return SafeZeroUtils.safeZero(totalFare)
      .plus(SafeZeroUtils.safeZero(roundUpAmount))
      .plus(SafeZeroUtils.safeZero(tip));
  }

  public Money getFareTotal() {
    return subTotal;
  }

  public Money getRideCost() {
    return SafeZeroUtils.safeZero(subTotal)
      .plus(SafeZeroUtils.safeZero(cityFee))
      .plus(SafeZeroUtils.safeZero(bookingFee))
      .plus(SafeZeroUtils.safeZero(processingFee));
  }

  public void reset() {
    this.airportFee = Constants.ZERO_USD;
    this.baseFare = Constants.ZERO_USD;
    this.bookingFee = Constants.ZERO_USD;
    this.cancellationFee = Constants.ZERO_USD;
    this.cityFee = Constants.ZERO_USD;
    this.distanceFare = Constants.ZERO_USD;
    this.driverPayment = Constants.ZERO_USD;
    this.freeCreditCharged = Constants.ZERO_USD;
    this.minimumFare = Constants.ZERO_USD;
    this.normalFare = Constants.ZERO_USD;
    this.processingFee = Constants.ZERO_USD;
    this.raPayment = Constants.ZERO_USD;
    this.ratePerMile = Constants.ZERO_USD;
    this.ratePerMinute = Constants.ZERO_USD;
    this.roundUpAmount = Constants.ZERO_USD;
    this.stripeCreditCharge = Constants.ZERO_USD;
    this.subTotal = Constants.ZERO_USD;
    this.surgeFare = Constants.ZERO_USD;
    this.timeFare = Constants.ZERO_USD;
    this.tip = Constants.ZERO_USD;
    this.totalFare = Constants.ZERO_USD;
  }

}
