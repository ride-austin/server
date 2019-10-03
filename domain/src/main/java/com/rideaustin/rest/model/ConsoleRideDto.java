package com.rideaustin.rest.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.Optional;

import org.joda.money.Money;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.querydsl.core.annotations.QueryProjection;
import com.rideaustin.Constants;
import com.rideaustin.model.enums.CardBrand;
import com.rideaustin.model.enums.RideStatus;
import com.rideaustin.model.ride.CityCarType;
import com.rideaustin.model.ride.FareDetails;
import com.rideaustin.utils.AppInfoUtils;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@ApiModel
public class ConsoleRideDto {

  @ApiModelProperty(required = true)
  private final long id;
  @ApiModelProperty(required = true)
  private final RideStatus status;
  @ApiModelProperty(required = true)
  private final double startLocationLat;
  @ApiModelProperty(required = true)
  private final double startLocationLong;
  @ApiModelProperty
  private final Double endLocationLat;
  @ApiModelProperty
  private final Double endLocationLong;
  @ApiModelProperty(required = true)
  private final String startAddress;
  @JsonIgnore
  @ApiModelProperty(hidden = true)
  private final FareDetails fareDetails;
  @Setter
  @ApiModelProperty
  private Date startedOn;
  @Setter
  @ApiModelProperty
  private Date driverAcceptedOn;
  @ApiModelProperty
  private final Date completedOn;
  @ApiModelProperty
  private final Date cancelledOn;
  @ApiModelProperty(required = true)
  private final Date requestedOn;
  @ApiModelProperty
  private final String endAddress;
  @ApiModelProperty
  private final ConsoleActiveDriverDto activeDriver;
  @ApiModelProperty(required = true)
  private final ConsoleRiderDto rider;
  @ApiModelProperty(required = true)
  private final ConsoleRiderCard riderCard;
  @ApiModelProperty(required = true)
  private final RequestedCarType requestedCarType;
  @Setter
  @ApiModelProperty
  private Date driverReachedOn;
  @ApiModelProperty
  private final Date tippedOn;
  @ApiModelProperty(required = true)
  private final BigDecimal surgeFactor;
  @ApiModelProperty
  private final BigDecimal distanceTravelled;
  @JsonIgnore
  @ApiModelProperty(hidden = true)
  private final long cityId;
  @Setter
  @JsonIgnore
  @ApiModelProperty(hidden = true)
  private Money totalChargeOverride;
  @Setter
  @ApiModelProperty
  private String campaign;
  @Setter
  @ApiModelProperty
  private Money campaignCoverage;

  @QueryProjection
  public ConsoleRideDto(long id, RideStatus status, double startLocationLat, double startLocationLong, Double endLocationLat,
    Double endLocationLong, String startAddress, FareDetails fareDetails, Date startedOn, Date driverAcceptedOn, Date completedOn,
    Date cancelledOn, Date requestedOn, String endAddress, long driverId, String driverPhotoUrl, String driverFullName, long riderId, String riderPhotoUrl,
    String riderFullName, CardBrand cardBrand, String cardNumber, String requestedCarType, Date driverReachedOn, Date tippedOn,
    BigDecimal surgeFactor, BigDecimal distanceTravelled, long cityId, String driverUA, String riderUA) {
    this.id = id;
    this.status = status;
    this.startLocationLat = startLocationLat;
    this.startLocationLong = startLocationLong;
    this.endLocationLat = endLocationLat;
    this.endLocationLong = endLocationLong;
    this.startAddress = startAddress;
    this.fareDetails = fareDetails;
    this.startedOn = startedOn;
    this.driverAcceptedOn = driverAcceptedOn;
    this.completedOn = completedOn;
    this.cancelledOn = cancelledOn;
    this.requestedOn = requestedOn;
    this.endAddress = endAddress;
    this.activeDriver = new ConsoleActiveDriverDto(driverId, driverPhotoUrl, driverFullName, AppInfoUtils.extractVersion(driverUA));
    this.rider = new ConsoleRiderDto(riderId, riderPhotoUrl, riderFullName, AppInfoUtils.extractVersion(riderUA));
    this.riderCard = new ConsoleRiderCard(cardBrand, cardNumber);
    this.requestedCarType = new RequestedCarType(requestedCarType);
    this.driverReachedOn = driverReachedOn;
    this.tippedOn = tippedOn;
    this.surgeFactor = surgeFactor;
    this.distanceTravelled = distanceTravelled;
    this.cityId = cityId;
  }

  @ApiModelProperty
  @JsonProperty("distanceTravelled")
  public BigDecimal getDistanceTravelledInMiles() {
    return distanceTravelled == null ? null :
      Constants.MILES_PER_METER.multiply(distanceTravelled).setScale(2, RoundingMode.HALF_UP);
  }

  @JsonProperty
  @ApiModelProperty
  public Money getTotalFare() {
    return fareDetails.getTotalFare();
  }

  @JsonProperty
  @ApiModelProperty
  public Money getMinimumFare() {
    return fareDetails.getMinimumFare();
  }

  @JsonProperty
  @ApiModelProperty
  public Money getBaseFare() {
    return fareDetails.getBaseFare();
  }

  @JsonProperty
  @ApiModelProperty
  public Money getRatePerMile() {
    return fareDetails.getRatePerMile();
  }

  @JsonProperty
  @ApiModelProperty
  public Money getRatePerMinute() {
    return fareDetails.getRatePerMinute();
  }

  @JsonProperty
  @ApiModelProperty
  public Money getEstimatedFare() {
    return fareDetails.getEstimatedFare();
  }

  @JsonProperty
  @ApiModelProperty
  public Money getBookingFee() {
    return fareDetails.getBookingFee();
  }

  @JsonProperty
  @ApiModelProperty
  public Money getDistanceFare() {
    return fareDetails.getDistanceFare();
  }

  @JsonProperty
  @ApiModelProperty
  public Money getTimeFare() {
    return fareDetails.getTimeFare();
  }

  @JsonProperty
  @ApiModelProperty
  public Money getCityFee() {
    return fareDetails.getCityFee();
  }

  @JsonProperty
  @ApiModelProperty
  public Money getCancellationFee() {
    return fareDetails.getCancellationFee();
  }

  @JsonProperty
  @ApiModelProperty
  public Money getProcessingFee() {
    return fareDetails.getProcessingFee();
  }

  @JsonProperty
  @ApiModelProperty
  public Money getSubTotal() {
    return fareDetails.getSubTotal();
  }

  @JsonProperty
  @ApiModelProperty
  public Money getNormalFare() {
    return fareDetails.getNormalFare();
  }

  @JsonProperty
  @ApiModelProperty
  public Money getSurgeFare() {
    return fareDetails.getSurgeFare();
  }

  @JsonProperty
  @ApiModelProperty
  public Money getFreeCreditCharged() {
    return fareDetails.getFreeCreditCharged();
  }

  @JsonProperty
  @ApiModelProperty
  public Money getStripeCreditCharge() {
    return fareDetails.getStripeCreditCharge();
  }

  @JsonProperty
  @ApiModelProperty
  public Money getDriverPayment() {
    return fareDetails.getDriverPayment();
  }

  @JsonProperty
  @ApiModelProperty
  public Money getRaPayment() {
    return fareDetails.getRaPayment();
  }

  @JsonProperty
  @ApiModelProperty
  public Money getTip() {
    return fareDetails.getTip();
  }

  @JsonProperty
  @ApiModelProperty
  public Money getRoundUpAmount() {
    return fareDetails.getRoundUpAmount();
  }

  @JsonProperty
  @ApiModelProperty
  public Money getAirportFee() {
    return fareDetails.getAirportFee();
  }

  @JsonProperty
  @ApiModelProperty
  public Money getFareTotal() {
    return fareDetails.getFareTotal();
  }

  @JsonProperty
  @ApiModelProperty
  public Money getTotalCharge() {
    return Optional.ofNullable(totalChargeOverride)
      .orElse(fareDetails.getTotalCharge());
  }

  @JsonProperty
  @ApiModelProperty
  public Money getRideCost() {
    return fareDetails.getRideCost();
  }

  @Getter
  @ApiModel
  private static final class ConsoleActiveDriverDto {
    @ApiModelProperty(required = true)
    private final ConsoleAvatarDto driver;

    ConsoleActiveDriverDto(long id, String photoUrl, String fullName, String version) {
      this.driver = new ConsoleAvatarDto(id, photoUrl, fullName, version);
    }

  }

  @Getter
  @ApiModel
  private static final class ConsoleRiderDto {
    @ApiModelProperty(required = true)
    private final ConsoleAvatarDto user;

    ConsoleRiderDto(long id, String photoUrl, String fullName, String version) {
      this.user = new ConsoleAvatarDto(id, photoUrl, fullName, version);
    }

    @JsonProperty
    @ApiModelProperty(required = true)
    public long getId() {
      return user.id;
    }

    @JsonProperty
    @ApiModelProperty(required = true)
    public String getFullName() {
      return user.fullName;
    }

  }

  @Getter
  @ApiModel
  @AllArgsConstructor
  private static final class ConsoleAvatarDto {
    @ApiModelProperty(required = true)
    private final long id;
    @ApiModelProperty(required = true)
    private final String photoUrl;
    @ApiModelProperty(required = true)
    private final String fullName;
    @ApiModelProperty(required = true)
    private final String appVersion;
  }

  @Getter
  @ApiModel
  @AllArgsConstructor
  private static final class ConsoleRiderCard {
    @ApiModelProperty(required = true)
    private final CardBrand cardBrand;
    @ApiModelProperty(required = true)
    private final String cardNumber;
  }

  @Getter
  @ApiModel
  @AllArgsConstructor
  @RequiredArgsConstructor
  public static final class RequestedCarType {
    @ApiModelProperty(required = true)
    private final String carCategory;
    @ApiModelProperty(required = true)
    private String title;
    @ApiModelProperty(required = true)
    private Money minimumFare;
    @ApiModelProperty(required = true)
    private Money ratePerMile;
    @ApiModelProperty(required = true)
    private Money ratePerMinute;

    public void fillInfo(CityCarType cityCarType) {
      this.title = cityCarType.getCarType().getTitle();
      this.minimumFare = cityCarType.getMinimumFare();
      this.ratePerMile = cityCarType.getRatePerMile();
      this.ratePerMinute = cityCarType.getRatePerMinute();
    }
  }
}