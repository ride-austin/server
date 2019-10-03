package com.rideaustin.rest.model;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.joda.money.Money;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.querydsl.core.annotations.QueryProjection;
import com.rideaustin.model.Address;
import com.rideaustin.model.enums.RideStatus;
import com.rideaustin.model.ride.DriverType;
import com.rideaustin.service.user.DriverTypeUtils;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@ApiModel
@JsonIgnoreProperties(ignoreUnknown = true)
public class MobileDriverRideDto {

  @ApiModelProperty(required = true)
  private final Long id;
  @JsonIgnore
  @ApiModelProperty(hidden = true)
  private final Long driverId;
  @ApiModelProperty(required = true)
  private final RideStatus status;
  @ApiModelProperty(required = true)
  private final Rider rider;
  @Setter
  @ApiModelProperty
  private Long estimatedTimeArrive;
  @ApiModelProperty(required = true)
  private final Double startLocationLat;
  @ApiModelProperty(required = true)
  private final Double startLocationLong;
  @ApiModelProperty(required = true)
  private final Double endLocationLat;
  @ApiModelProperty(required = true)
  private final Double endLocationLong;
  @ApiModelProperty(required = true)
  private final String startAddress;
  @Setter
  @ApiModelProperty
  private String endAddress;
  @ApiModelProperty(required = true)
  private final Address start;
  @Setter
  @ApiModelProperty
  private Address end;
  @ApiModelProperty(required = true)
  private final BigDecimal surgeFactor;
  @ApiModelProperty(required = true)
  private final Money driverPayment;
  @ApiModelProperty(required = true)
  private final RequestedCarType requestedCarType;
  @ApiModelProperty(required = true)
  private final RequestedDriverType requestedDriverType;
  @ApiModelProperty(required = true)
  private final Set<RequestedDriverType> requestedDriverTypes;
  @Setter
  @ApiModelProperty
  private String comment;
  @Setter
  @ApiModelProperty
  private RideUpgradeRequestDto upgradeRequest;
  @Setter
  @ApiModelProperty
  private MobileDriverRideDto nextRide;
  @ApiModelProperty
  private final Money freeCreditCharged;
  @ApiModelProperty
  private final String mapUrl;
  @Setter
  @ApiModelProperty(required = true)
  private RequestedDispatchType requestedDispatchType;

  @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
  public MobileDriverRideDto(@JsonProperty("id") Long id, @JsonProperty("status") RideStatus status,
    @JsonProperty("rider") Rider rider, @JsonProperty("startLocationLat") Double startLocationLat,
    @JsonProperty("startLocationLong") Double startLocationLong, @JsonProperty("endLocationLat") Double endLocationLat,
    @JsonProperty("endLocationLong") Double endLocationLong, @JsonProperty("startAddress") String startAddress,
    @JsonProperty("endAddress") String endAddress, @JsonProperty("start") Address start, @JsonProperty("end") Address end,
    @JsonProperty("surgeFactor") BigDecimal surgeFactor, @JsonProperty("driverPayment") Money driverPayment,
    @JsonProperty("requestedCarType") RequestedCarType requestedCarType,
    @JsonProperty("requestedDriverType") RequestedDriverType requestedDriverType,
    @JsonProperty("requestedDriverTypes") Set<RequestedDriverType> requestedDriverTypes,
    @JsonProperty("freeCreditCharged") Money freeCreditCharged, @JsonProperty("mapUrl") String mapUrl) {
    this.id = id;
    this.driverId = null;
    this.status = status;
    this.rider = rider;
    this.startLocationLat = startLocationLat;
    this.startLocationLong = startLocationLong;
    this.endLocationLat = endLocationLat;
    this.endLocationLong = endLocationLong;
    this.startAddress = startAddress;
    this.endAddress = endAddress;
    this.start = start;
    this.end = end;
    this.surgeFactor = surgeFactor;
    this.driverPayment = driverPayment;
    this.requestedCarType = requestedCarType;
    this.requestedDriverType = requestedDriverType;
    this.requestedDriverTypes = requestedDriverTypes;
    this.freeCreditCharged = freeCreditCharged;
    this.mapUrl = mapUrl;
  }

  @QueryProjection
  public MobileDriverRideDto(Long id, Long driverId, RideStatus status, Long riderId, String photoUrl, String firstname,
    String lastname, String phoneNumber, String email, double rating, Double startLocationLat, Double startLocationLong,
    Double endLocationLat, Double endLocationLong, String startAddress, String endAddress, Address start, Address end,
    BigDecimal surgeFactor, Money driverPayment, String title, String carCategory, String plainIconUrl, String configuration,
    Integer requestedDriverTypeBitmask, String comment, Money freeCreditCharged, String mapUrl) {
    this.id = id;
    this.driverId = driverId;
    this.status = status;
    this.requestedDriverType = DriverTypeUtils.fromBitMask(requestedDriverTypeBitmask).stream()
      .filter(d -> DriverType.DIRECT_CONNECT.equals(d) || DriverType.WOMEN_ONLY.equals(d))
      .findAny()
      .map(RequestedDriverType::new)
      .orElse(null);
    this.requestedDriverTypes = DriverTypeUtils.fromBitMask(requestedDriverTypeBitmask).stream()
      .map(RequestedDriverType::new)
      .collect(Collectors.toSet());
    this.rider = new Rider(riderId, photoUrl, firstname, lastname, phoneNumber, email, rating);
    this.startLocationLat = startLocationLat;
    this.startLocationLong = startLocationLong;
    this.endLocationLat = endLocationLat;
    this.endLocationLong = endLocationLong;
    this.startAddress = startAddress;
    this.endAddress = endAddress;
    this.start = start;
    this.end = Optional.ofNullable(end).orElse(new Address());
    this.surgeFactor = surgeFactor;
    this.driverPayment = driverPayment;
    this.requestedCarType = new RequestedCarType(title, carCategory, plainIconUrl, configuration);
    this.comment = comment;
    this.freeCreditCharged = freeCreditCharged;
    this.mapUrl = mapUrl;
  }

  @Getter
  @ApiModel
  public static class Rider {
    @ApiModelProperty(required = true)
    private final Long id;
    @ApiModelProperty(required = true)
    private final User user;
    @ApiModelProperty(required = true)
    private final String firstname;
    @ApiModelProperty(required = true)
    private final String lastname;
    @ApiModelProperty(required = true)
    private final String fullName;
    @ApiModelProperty(required = true)
    private final String phoneNumber;
    @ApiModelProperty(required = true)
    private final String email;
    @ApiModelProperty(required = true)
    private final double rating;

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public Rider(@JsonProperty("id") Long id, @JsonProperty("photoUrl") String photoUrl, @JsonProperty("firstname") String firstname,
      @JsonProperty("lastname") String lastname, @JsonProperty("phoneNumber") String phoneNumber, @JsonProperty("email") String email,
      @JsonProperty("rating") double rating) {
      this.id = id;
      this.user = new User(photoUrl);
      this.firstname = firstname;
      this.lastname = lastname;
      this.fullName = String.format("%s %s", firstname, lastname);
      this.phoneNumber = phoneNumber;
      this.email = email;
      this.rating = rating;
    }

    @Getter
    @ApiModel
    private static class User {
      @ApiModelProperty
      private final String photoUrl;

      @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
      public User(@JsonProperty("photoUrl") String photoUrl) {
        this.photoUrl = photoUrl;
      }
    }
  }

  @Getter
  @ApiModel
  public static class RequestedCarType {
    @ApiModelProperty(required = true)
    private final String title;
    @ApiModelProperty(required = true)
    private final String carCategory;
    @ApiModelProperty(required = true)
    private final String plainIconUrl;
    @ApiModelProperty(required = true)
    private final String configuration;

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public RequestedCarType(@JsonProperty("title") String title, @JsonProperty("carCategory") String carCategory,
      @JsonProperty("plainIconUrl") String plainIconUrl, @JsonProperty("configuration") String configuration) {
      this.title = title;
      this.carCategory = carCategory;
      this.plainIconUrl = plainIconUrl;
      this.configuration = configuration;
    }
  }

  @Getter
  @ApiModel
  private static class RequestedDriverType {
    @ApiModelProperty(required = true)
    private final String name;

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public RequestedDriverType(@JsonProperty("name") String name) {
      this.name = name;
    }
  }

  public enum RequestedDispatchType {
    REGULAR,
    DIRECT_CONNECT
  }
}
