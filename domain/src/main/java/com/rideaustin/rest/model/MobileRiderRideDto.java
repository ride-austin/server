package com.rideaustin.rest.model;

import static com.rideaustin.utils.SafeZeroUtils.safeZero;

import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.joda.money.Money;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.querydsl.core.annotations.QueryProjection;
import com.rideaustin.model.Address;
import com.rideaustin.model.DistanceAware;
import com.rideaustin.model.DrivingTimeAware;
import com.rideaustin.model.LocationAware;
import com.rideaustin.model.enums.DocumentType;
import com.rideaustin.model.enums.PaymentProvider;
import com.rideaustin.model.enums.RideStatus;
import com.rideaustin.service.location.model.LocationObject;
import com.rideaustin.service.user.CarTypesUtils;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@ApiModel
@JsonIgnoreProperties(ignoreUnknown = true)
public class MobileRiderRideDto {

  @ApiModelProperty(required = true)
  private final long id;
  @ApiModelProperty(required = true)
  private final RideStatus status;
  @Setter
  @ApiModelProperty
  private ActiveDriverDto activeDriver;
  @Setter
  @ApiModelProperty
  private Date driverAcceptedOn;
  @ApiModelProperty
  private final Date completedOn;
  @Setter
  @ApiModelProperty
  private Money tip;
  @ApiModelProperty
  private final Double driverRating;
  @Setter
  @ApiModelProperty
  private Long estimatedTimeArrive;
  @Setter
  @ApiModelProperty
  private Money upfrontCharge;
  @ApiModelProperty(required = true)
  private final Double startLocationLat;
  @ApiModelProperty(required = true)
  private final Double startLocationLong;
  @ApiModelProperty
  private final Double endLocationLat;
  @ApiModelProperty
  private final Double endLocationLong;
  @ApiModelProperty
  private final String startAddress;
  @ApiModelProperty
  private final String endAddress;
  @ApiModelProperty
  private final Address start;
  @ApiModelProperty
  private final Address end;
  @Setter
  @ApiModelProperty
  private PaymentProvider paymentProvider;
  @Setter
  @ApiModelProperty
  private Money totalFare;
  @ApiModelProperty
  private final Money driverPayment;
  @JsonIgnore
  @ApiModelProperty(hidden = true)
  private final String carType;
  @Setter
  @ApiModelProperty(required = true)
  private CityCarTypeDto requestedCarType;
  @Setter
  @ApiModelProperty
  private String comment;
  @Setter
  @ApiModelProperty
  private RideUpgradeRequestDto upgradeRequest;
  @ApiModelProperty
  private final Money freeCreditCharged;
  @ApiModelProperty
  private final String mapUrl;
  @Setter
  @ApiModelProperty
  private PrecedingRide precedingRide;
  @JsonIgnore
  @ApiModelProperty(hidden = true)
  private final Long cityId;
  @JsonIgnore
  @ApiModelProperty(hidden = true)
  private final Money roundUpAmount;
  @JsonIgnore
  @ApiModelProperty(hidden = true)
  private final Long riderId;
  @Setter
  @ApiModelProperty
  private Date freeCancellationExpiresOn;
  @Setter
  @ApiModelProperty
  private Date estimatedTimeCompletion;
  @Setter
  @ApiModelProperty(required = true)
  private boolean tippingAllowed;
  @Setter
  @ApiModelProperty
  private Date tipUntil;

  @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
  public MobileRiderRideDto(@JsonProperty("id") long id, @JsonProperty("status") RideStatus status,
    @JsonProperty("activeDriver") ActiveDriverDto activeDriver, @JsonProperty("completedOn") Date completedOn,
    @JsonProperty("driverRating") Double driverRating, @JsonProperty("startLocationLat") Double startLocationLat,
    @JsonProperty("startLocationLong") Double startLocationLong, @JsonProperty("endLocationLat") Double endLocationLat,
    @JsonProperty("endLocationLong") Double endLocationLong, @JsonProperty("startAddress") String startAddress,
    @JsonProperty("endAddress") String endAddress, @JsonProperty("start") Address start, @JsonProperty("end") Address end,
    @JsonProperty("totalFare") Money totalFare, @JsonProperty("driverPayment") Money driverPayment,
    @JsonProperty("freeCreditCharged") Money freeCreditCharged, @JsonProperty("mapUrl") String mapUrl,
    @JsonProperty("upfrontCharge") Money upfrontCharge, @JsonProperty("paymentProvider") PaymentProvider paymentProvider) {
    this.id = id;
    this.status = status;
    this.activeDriver = activeDriver;
    this.completedOn = completedOn;
    this.driverRating = driverRating;
    this.startLocationLat = startLocationLat;
    this.startLocationLong = startLocationLong;
    this.endLocationLat = endLocationLat;
    this.endLocationLong = endLocationLong;
    this.startAddress = startAddress;
    this.endAddress = endAddress;
    this.start = start;
    this.paymentProvider = paymentProvider;
    if (this.start != null) {
      this.start.setLat(startLocationLat);
      this.start.setLng(startLocationLong);
    }
    this.end = end;
    if (this.end != null) {
      this.end.setLat(endLocationLat);
      this.end.setLng(endLocationLong);
    }
    this.totalFare = totalFare;
    this.driverPayment = driverPayment;
    this.freeCreditCharged = freeCreditCharged;
    this.mapUrl = mapUrl;
    this.upfrontCharge = upfrontCharge;
    this.carType = null;
    this.cityId = null;
    this.roundUpAmount = null;
    this.riderId = null;
  }

  @QueryProjection
  public MobileRiderRideDto(long id, Long riderId, RideStatus status, Date driverAcceptedOn, Date completedOn, Money tip,
    Double driverRating, Money upfrontCharge, Double startLocationLat, Double startLocationLong, Double endLocationLat, Double endLocationLong,
    String startAddress, String endAddress, Address start, Address end, Money totalFare, Money driverPayment,
    String carType, String comment, Money freeCreditCharged, String mapUrl, Long cityId, Money roundUpAmount,
    Long activeDriverId, Long driverId, Double rating, Integer grantedDriverTypesBitmask, Long userId, String email,
    String firstname, String lastname, String phoneNumber, Boolean active, Long carId, String color, String license, String make,
    String model, String year, Integer carCategoriesBitmask) {
    this.id = id;
    this.status = status;
    this.driverAcceptedOn = driverAcceptedOn;
    this.completedOn = completedOn;
    this.tip = tip;
    this.upfrontCharge = upfrontCharge;
    this.driverRating = driverRating;
    this.startLocationLat = startLocationLat;
    this.startLocationLong = startLocationLong;
    this.endLocationLat = endLocationLat;
    this.endLocationLong = endLocationLong;
    this.startAddress = startAddress;
    this.endAddress = endAddress;
    this.start = Optional.ofNullable(start).orElse(new Address());
    this.end = Optional.ofNullable(end).orElse(new Address());
    this.start.setLat(startLocationLat);
    this.start.setLng(startLocationLong);
    this.end.setLat(endLocationLat);
    this.end.setLng(endLocationLong);
    this.totalFare = totalFare;
    this.driverPayment = driverPayment;
    this.carType = carType;
    this.comment = comment;
    this.freeCreditCharged = freeCreditCharged;
    this.mapUrl = mapUrl;
    this.cityId = cityId;
    this.roundUpAmount = roundUpAmount;
    this.riderId = riderId;
    if (activeDriverId != null) {
      this.activeDriver = new ActiveDriverDto(activeDriverId, driverId, rating, grantedDriverTypesBitmask, userId, email,
        firstname, lastname, null, phoneNumber, active, carId, color, license, make, model, year, carCategoriesBitmask);
    } else {
      this.activeDriver = null;
    }
  }

  @JsonProperty
  @ApiModelProperty
  public Money getTotalCharge() {
    return safeZero(getTotalFare())
      .plus(safeZero(getTip()))
      .plus(safeZero(getRoundUpAmount()));
  }

  @Getter
  @ApiModel
  public static class ActiveDriverDto implements LocationAware, DistanceAware, DrivingTimeAware {
    @ApiModelProperty(required = true)
    private final long id;
    @Setter
    @ApiModelProperty(required = true)
    private double latitude;
    @Setter
    @ApiModelProperty(required = true)
    private double longitude;
    @ApiModelProperty(required = true)
    private final MobileRiderDriverDto driver;
    @ApiModelProperty(required = true)
    private final MobileRiderCarDto selectedCar;
    @Setter
    @ApiModelProperty(required = true)
    private double course;
    @Setter
    @ApiModelProperty(required = true)
    private Long drivingTimeToRider;
    @ApiModelProperty(required = true)
    private final Set<String> carCategories;

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public ActiveDriverDto(@JsonProperty("id") long id, @JsonProperty("driver") MobileRiderDriverDto driver,
      @JsonProperty("selectedCar") MobileRiderCarDto selectedCar, @JsonProperty("carCategories") Set<String> carCategories) {
      this.id = id;
      this.driver = driver;
      this.selectedCar = selectedCar;
      this.carCategories = carCategories;
    }

    @QueryProjection
    public ActiveDriverDto(long id, long driverId, Double rating, Integer grantedDriverTypesBitmask, long userId, String email,
      String firstname, String lastname, String nickName, String phoneNumber, boolean active, Long carId, String color,
      String license, String make, String model, String year, Integer carCategories) {
      this.id = id;
      this.selectedCar = Optional.ofNullable(carId).map(c -> new MobileRiderCarDto(c, color, license, make, model, year)).orElse(null);
      this.driver = new MobileRiderDriverDto(driverId, rating, grantedDriverTypesBitmask, userId, email, firstname, lastname, nickName,
        phoneNumber, active, selectedCar);
      this.carCategories = Optional.ofNullable(carCategories).map(CarTypesUtils::fromBitMask).orElse(Collections.emptySet());
    }

    @Override
    @JsonIgnore
    @ApiModelProperty(hidden = true)
    public LocationObject getLocationObject() {
      LocationObject locationObject = new LocationObject();
      locationObject.setLatitude(latitude);
      locationObject.setLongitude(longitude);
      return locationObject;
    }

    @Override
    public void setLocationObject(LocationObject locationObject) {
      //do nothing
    }

    @Override
    @JsonIgnore
    public Double getDirectDistanceToRider() {
      throw new UnsupportedOperationException();
    }

    @Override
    public void setDirectDistanceToRider(Double directDistanceToRider) {
      //do nothing
    }

    @Override
    @JsonIgnore
    public Long getDrivingDistanceToRider() {
      return Long.MIN_VALUE;
    }

    @Override
    public void setDrivingDistanceToRider(Long drivingDistanceToRider) {
      //do nothing
    }

    @ApiModel
    public static class MobileRiderDriverDto extends MobileDriverDto {
      @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
      public MobileRiderDriverDto(@JsonProperty("id") long id, @JsonProperty("rating") Double rating,
        @JsonProperty("grantedDriverTypesBitmask") Integer grantedDriverTypesBitmask, @JsonProperty("userId") long userId,
        @JsonProperty("email") String email, @JsonProperty("firstname") String firstname,
        @JsonProperty("lastname") String lastname, @JsonProperty("nickName") String nickName,
        @JsonProperty("phoneNumber") String phoneNumber, @JsonProperty("active") boolean active,
        @JsonProperty("selectedCar") MobileCar selectedCar) {
        super(id, rating, grantedDriverTypesBitmask, userId, email, firstname, lastname, nickName, phoneNumber, active);
        setCars(Collections.singletonList(selectedCar));
      }

    }

    @Getter
    @ApiModel
    public static class MobileRiderCarDto extends MobileDriverDto.MobileCar {

      @Setter
      @ApiModelProperty(required = true)
      private Map<DocumentType, String> carPhotos;

      public MobileRiderCarDto(long id, String color, String license, String make, String model, String year) {
        super(id, color, license, make, model, year);
      }

      @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
      public MobileRiderCarDto(@JsonProperty("id") long id, @JsonProperty("color") String color, @JsonProperty("license") String license,
        @JsonProperty("make") String make, @JsonProperty("model") String model, @JsonProperty("year") String year,
        @JsonProperty("carPhotos") Map<DocumentType, String> carPhotos) {
        super(id, color, license, make, model, year);
        this.carPhotos = carPhotos;
      }
    }
  }

  @Getter
  @ApiModel
  public static class PrecedingRide {
    @ApiModelProperty(required = true)
    private final long id;
    @ApiModelProperty(required = true)
    private final RideStatus status;
    @ApiModelProperty(required = true)
    private final EndAddress end;

    @QueryProjection
    public PrecedingRide(long id, RideStatus status, String address, String zipCode, double lat, double lng) {
      this.id = id;
      this.status = status;
      this.end = new EndAddress(address, zipCode, lat, lng);
    }

    @JsonProperty
    @ApiModelProperty
    public String getStartAddress() {
      return end.getAddress();
    }

    @JsonProperty
    @ApiModelProperty(required = true)
    public double getStartLocationLat() {
      return end.getLatitude();
    }

    @JsonProperty
    @ApiModelProperty(required = true)
    public double getStartLocationLong() {
      return end.getLongitude();
    }

    @Getter
    @ApiModel
    @RequiredArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static final class EndAddress {
      @ApiModelProperty
      private final String address;
      @ApiModelProperty
      private final String zipCode;
      @ApiModelProperty(required = true)
      private final double latitude;
      @ApiModelProperty(required = true)
      private final double longitude;
    }
  }
}
