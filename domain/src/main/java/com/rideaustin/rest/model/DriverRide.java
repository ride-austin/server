package com.rideaustin.rest.model;

import java.math.BigDecimal;
import java.util.Date;

import org.joda.money.Money;

import com.querydsl.core.annotations.QueryProjection;
import com.rideaustin.model.Address;
import com.rideaustin.model.enums.RideStatus;
import com.rideaustin.model.ride.CarType;
import com.rideaustin.model.ride.FareDetails;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@ApiModel
public class DriverRide {

  @ApiModelProperty(required = true)
  private final Long id;
  @ApiModelProperty(required = true)
  private final RideStatus status;
  @ApiModelProperty(required = true)
  private final Double startLocationLat;
  @ApiModelProperty(required = true)
  private final Double startLocationLng;
  @ApiModelProperty(required = true)
  private final Address start;
  @ApiModelProperty(required = true)
  private final Double endLocationLat;
  @ApiModelProperty(required = true)
  private final Double endLocationLng;
  @ApiModelProperty(required = true)
  private final Address end;
  @ApiModelProperty(required = true)
  private final Date startedOn;
  @ApiModelProperty(required = true)
  private final Date completedOn;
  @ApiModelProperty(required = true)
  private final Date cancelledOn;
  @ApiModelProperty(required = true)
  private final Money minimumFare;
  @ApiModelProperty(required = true)
  private final Money baseFare;
  @ApiModelProperty(required = true)
  private final Money ratePerMile;
  @ApiModelProperty(required = true)
  private final Money ratePerMinute;
  @ApiModelProperty(required = true)
  private final Money distanceFare;
  @ApiModelProperty(required = true)
  private final Money timeFare;
  @ApiModelProperty(required = true)
  private final String rideMap;
  @ApiModelProperty(required = true)
  private final Money cityFee;
  @ApiModelProperty(required = true)
  private final Money raFee;
  @ApiModelProperty(required = true)
  private final Money subTotal;
  @ApiModelProperty(required = true)
  private final Money totalFare;
  @ApiModelProperty(required = true)
  private final Money bookingFee;
  @ApiModelProperty(required = true)
  private final Money driverPayment;
  @ApiModelProperty(required = true)
  private final CarType requestedCarType;
  @ApiModelProperty(required = true)
  private final RideCar car;
  @ApiModelProperty(required = true)
  private final Money roundUpAmount;
  @ApiModelProperty(required = true)
  private final Double driverRating;
  @ApiModelProperty(required = true)
  private final Money tip;
  @ApiModelProperty(required = true)
  private final BigDecimal surgeFactor;
  @ApiModelProperty(required = true)
  private final Money surgeFare;

  @QueryProjection
  public DriverRide(long id, RideStatus status, double startLocationLat, double startLocationLng,
    Address start, double endLocationLat, double endLocationLng, Address end, Date startedOn, Date completedOn,
    Date cancelledOn, FareDetails fareDetails, double driverRating, CarType carType, String rideMap,
    BigDecimal surgeFactor, String carMake, String carModel) {
    this.id = id;
    this.status = status;
    this.startLocationLat = startLocationLat;
    this.startLocationLng = startLocationLng;
    this.start = start;
    this.endLocationLat = endLocationLat;
    this.endLocationLng = endLocationLng;
    this.end = end;
    this.startedOn = startedOn;
    this.completedOn = completedOn;
    this.cancelledOn = cancelledOn;
    this.minimumFare = fareDetails.getMinimumFare();
    this.baseFare = fareDetails.getBaseFare();
    this.ratePerMile = fareDetails.getRatePerMile();
    this.ratePerMinute = fareDetails.getRatePerMinute();
    this.distanceFare = fareDetails.getDistanceFare();
    this.timeFare = fareDetails.getTimeFare();
    this.rideMap = rideMap;
    this.roundUpAmount = fareDetails.getRoundUpAmount();
    this.driverPayment = fareDetails.getDriverPayment();
    this.requestedCarType = carType;
    this.subTotal = fareDetails.getSubTotal();
    this.totalFare = fareDetails.getTotalFare();
    this.raFee = fareDetails.getRaPayment();
    this.cityFee = fareDetails.getCityFee();
    this.bookingFee = fareDetails.getBookingFee();
    this.driverRating = driverRating;
    this.tip = fareDetails.getTip();
    this.surgeFare = fareDetails.getSurgeFare();
    this.surgeFactor = surgeFactor;
    this.car = new RideCar(carMake, carModel);
  }

  @Getter
  @ApiModel
  @RequiredArgsConstructor
  static class RideCar {

    @ApiModelProperty(required = true)
    private final String make;
    @ApiModelProperty(required = true)
    private final String model;

  }

}