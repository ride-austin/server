package com.rideaustin.model.ride;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.EnumSet;
import java.util.Optional;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Embedded;
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

import org.hibernate.annotations.DynamicUpdate;
import org.joda.money.Money;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.querydsl.core.annotations.QueryInit;
import com.rideaustin.Constants;
import com.rideaustin.model.Address;
import com.rideaustin.model.BaseEntity;
import com.rideaustin.model.Charity;
import com.rideaustin.model.Session;
import com.rideaustin.model.enums.PaymentStatus;
import com.rideaustin.model.enums.RideStatus;
import com.rideaustin.model.helper.CommentConverter;
import com.rideaustin.model.user.Rider;
import com.rideaustin.rest.model.RideEndLocation;
import com.rideaustin.rest.model.RideStartLocation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Builder
@DynamicUpdate
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "rides")
@JsonIgnoreProperties(ignoreUnknown = true)
public class Ride extends BaseEntity {

  @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
  @JoinColumn(name = "active_driver_id")
  @QueryInit("*.*.*.*.*")
  private ActiveDriver activeDriver;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "rider_id")
  @QueryInit("*.*.*.*.*")
  private Rider rider;

  @Getter
  @Setter
  @OneToOne(mappedBy = "ride", fetch = FetchType.LAZY)
  private RiderOverride riderOverride;

  @Enumerated(EnumType.STRING)
  private RideStatus status;

  @Enumerated(EnumType.STRING)
  @Column(name = "payment_status")
  @JsonIgnore
  private PaymentStatus paymentStatus;

  @Column(name = "distance_travelled")
  @JsonIgnore
  private BigDecimal distanceTravelled;

  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "requested_on", nullable = false)
  private Date requestedOn;

  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "started_on")
  private Date startedOn;

  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "driver_reached_on")
  private Date driverReachedOn;

  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "driver_accepted_on")
  private Date driverAcceptedOn;

  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "completed_on")
  private Date completedOn;

  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "cancelled_on")
  private Date cancelledOn;

  @Column(name = "surge_factor")
  private BigDecimal surgeFactor = BigDecimal.ONE;

  @Column(name = "start_location_lat")
  private Double startLocationLat;

  @Column(name = "start_location_long")
  private Double startLocationLong;

  @Column(name = "end_location_lat")
  private Double endLocationLat;

  @Column(name = "end_location_long")
  private Double endLocationLong;

  @Column(name = "start_area_id")
  private Long startAreaId;

  @Column(name = "apple_pay_token")
  private String applePayToken;

  @Column(name = "comment", columnDefinition = "text")
  @Convert(converter = CommentConverter.class)
  private String comment;

  @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
  @JoinColumn(name = "driver_session_id")
  @JsonIgnore
  private Session driverSession;

  @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
  @JoinColumn(name = "rider_session_id")
  @JsonIgnore
  private Session riderSession;

  @Embedded
  @AttributeOverrides({
    @AttributeOverride(name = "address", column = @Column(name = "start_address")),
    @AttributeOverride(name = "zipCode", column = @Column(name = "start_zip_code"))
  })
  private Address start = new Address();

  @Embedded
  @AttributeOverrides({
    @AttributeOverride(name = "address", column = @Column(name = "end_address")),
    @AttributeOverride(name = "zipCode", column = @Column(name = "end_zip_code"))
  })
  private Address end = new Address();

  @Column(name = "driver_rating")
  private Double driverRating;

  @Column(name = "rider_rating")
  private Double riderRating;

  @Column(name = "ride_map")
  private String rideMap;

  @Column(name = "ride_map_minimized")
  private String rideMapMinimized;

  @Column(name = "charge_id")
  private String chargeId;

  @Column(name = "pre_charge_id")
  private String preChargeId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "charity_id")
  private Charity charity;

  @ManyToOne
  @JoinColumn(name = "requested_car_category")
  private CarType requestedCarType;

  @Column(name = "requested_driver_type_bitmask")
  private Integer requestedDriverTypeBitmask;

  @Column(name = "tracking_share_token")
  private String trackingShareToken;

  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "tipped_on")
  private Date tippedOn;

  @Column(name = "city_id")
  private Long cityId;

  @Column(name = "promocode_redemption_id")
  private Long promocodeRedemptionId;

  @Column(name = "airport_id")
  private Long airportId;

  @Embedded
  @JsonIgnore
  private FareDetails fareDetails = new FareDetails();

  @JsonIgnore
  public boolean isSurgeRide() {
    return getSurgeFactor() != null && Constants.NEUTRAL_SURGE_FACTOR.compareTo(getSurgeFactor()) < 0;
  }

  @JsonIgnore
  public BigDecimal getDuration() {
    Long started = Optional.ofNullable(startedOn).map(Date::getTime).orElse(new Date().getTime());
    Long completed = Optional.ofNullable(completedOn).map(Date::getTime).orElse(new Date().getTime());
    return BigDecimal.valueOf(completed - started);
  }

  @JsonIgnore
  public boolean isUserCancelled() {
    return EnumSet.of(RideStatus.RIDER_CANCELLED, RideStatus.DRIVER_CANCELLED).contains(status);
  }

  @JsonIgnore
  public void fillEndLocation(RideEndLocation endLocation, Address end) {
    this.setEndLocationLat(endLocation.getLat());
    this.setEndLocationLong(endLocation.getLng());
    if (end != null) {
      this.setEnd(end);
    }
  }

  @JsonIgnore
  public void fillStartLocation(RideStartLocation startLocation, Address start) {
    this.setStartLocationLat(startLocation.getLat());
    this.setStartLocationLong(startLocation.getLng());
    if (start != null) {
      this.setStart(start);
    }
  }

  public ActiveDriver getActiveDriver() {
    return activeDriver;
  }

  public void setActiveDriver(ActiveDriver activeDriver) {
    this.activeDriver = activeDriver;
  }

  public Rider getRider() {
    return rider;
  }

  public void setRider(Rider rider) {
    this.rider = rider;
  }

  public RideStatus getStatus() {
    return status;
  }

  public void setStatus(RideStatus status) {
    this.status = status;
  }

  public BigDecimal getDistanceTravelled() {
    return distanceTravelled;
  }

  public void setDistanceTravelled(BigDecimal distanceTravelled) {
    this.distanceTravelled = distanceTravelled;
  }

  @JsonProperty("distanceTravelled")
  public BigDecimal getDistanceTravelledInMiles() {
    return distanceTravelled == null ? null :
      Constants.MILES_PER_METER.multiply(distanceTravelled).setScale(2, RoundingMode.HALF_UP);
  }

  public Date getStartedOn() {
    return startedOn;
  }

  public void setStartedOn(Date startedOn) {
    this.startedOn = startedOn;
  }

  public Date getDriverReachedOn() {
    return driverReachedOn;
  }

  public void setDriverReachedOn(Date driverReachedOn) {
    this.driverReachedOn = driverReachedOn;
  }

  public Date getDriverAcceptedOn() {
    return driverAcceptedOn;
  }

  public void setDriverAcceptedOn(Date driverAcceptedOn) {
    this.driverAcceptedOn = driverAcceptedOn;
  }

  public Date getCompletedOn() {
    return completedOn;
  }

  public void setCompletedOn(Date completedOn) {
    this.completedOn = completedOn;
  }

  public Double getStartLocationLat() {
    return startLocationLat;
  }

  public void setStartLocationLat(Double startLocationLat) {
    this.startLocationLat = startLocationLat;
  }

  public Double getStartLocationLong() {
    return startLocationLong;
  }

  public void setStartLocationLong(Double startLocationLong) {
    this.startLocationLong = startLocationLong;
  }

  public Double getEndLocationLat() {
    return endLocationLat;
  }

  public void setEndLocationLat(Double endLocationLat) {
    this.endLocationLat = endLocationLat;
  }

  public Double getEndLocationLong() {
    return endLocationLong;
  }

  public void setEndLocationLong(Double endLocationLong) {
    this.endLocationLong = endLocationLong;
  }

  public Double getDriverRating() {
    return driverRating;
  }

  public void setDriverRating(Double driverRating) {
    this.driverRating = driverRating;
  }

  public Double getRiderRating() {
    return riderRating;
  }

  public void setRiderRating(Double riderRating) {
    this.riderRating = riderRating;
  }

  public String getRideMap() {
    return rideMap;
  }

  public void setRideMap(String rideMap) {
    this.rideMap = rideMap;
  }

  public String getRideMapMinimized() {
    return rideMapMinimized;
  }

  public void setRideMapMinimized(String rideMapMinimized) {
    this.rideMapMinimized = rideMapMinimized;
  }

  public String getChargeId() {
    return chargeId;
  }

  public void setChargeId(String chargeId) {
    this.chargeId = chargeId;
  }

  public String getPreChargeId() {
    return preChargeId;
  }

  public void setPreChargeId(String preChargeId) {
    this.preChargeId = preChargeId;
  }

  /**
   * @deprecated replaced by {@link #getStart()} getAddress()}
   */
  @JsonProperty
  @Deprecated
  public String getStartAddress() {
    return getStart().getAddress();
  }

  public Address getStart() {
    if (start == null) {
      start = new Address();
    }
    return start;
  }

  public void setStart(Address start) {
    this.start = start;
  }

  public Address getEnd() {
    if (end == null) {
      end = new Address();
    }
    return end;
  }

  public void setEnd(Address end) {
    this.end = end;
  }

  /**
   * @deprecated replaced by {@link #getEnd()}  getAddress()}
   */
  @JsonProperty
  @Deprecated
  public String getEndAddress() {
    return getEnd().getAddress();
  }

  public Charity getCharity() {
    return charity;
  }

  public void setCharity(Charity charity) {
    this.charity = charity;
  }

  public CarType getRequestedCarType() {
    return requestedCarType;
  }

  public void setRequestedCarType(CarType requestedCarType) {
    this.requestedCarType = requestedCarType;
  }

  public Session getDriverSession() {
    return driverSession;
  }

  public void setDriverSession(Session driverSession) {
    this.driverSession = driverSession;
  }

  public Session getRiderSession() {
    return riderSession;
  }

  public void setRiderSession(Session riderSession) {
    this.riderSession = riderSession;
  }

  public BigDecimal getSurgeFactor() {
    return surgeFactor;
  }

  public void setSurgeFactor(BigDecimal surgeFactor) {
    this.surgeFactor = surgeFactor;
  }

  public Date getCancelledOn() {
    return cancelledOn;
  }

  public void setCancelledOn(Date cancelledOn) {
    this.cancelledOn = cancelledOn;
  }

  public PaymentStatus getPaymentStatus() {
    return paymentStatus;
  }

  public void setPaymentStatus(PaymentStatus paymentStatus) {
    this.paymentStatus = paymentStatus;
  }

  public String getTrackingShareToken() {
    return trackingShareToken;
  }

  public void setTrackingShareToken(String trackingShareToken) {
    this.trackingShareToken = trackingShareToken;
  }

  public Date getRequestedOn() {
    return requestedOn;
  }

  public void setRequestedOn(Date requestedOn) {
    this.requestedOn = requestedOn;
  }

  public Date getTippedOn() {
    return tippedOn;
  }

  public void setTippedOn(Date tippedOn) {
    this.tippedOn = tippedOn;
  }

  public Integer getRequestedDriverTypeBitmask() {
    return requestedDriverTypeBitmask;
  }

  public void setRequestedDriverTypeBitmask(Integer requestedDriverTypeBitmask) {
    this.requestedDriverTypeBitmask = requestedDriverTypeBitmask;
  }

  public Long getCityId() {
    return cityId;
  }

  public void setCityId(Long cityId) {
    this.cityId = cityId;
  }

  public Long getStartAreaId() {
    return startAreaId;
  }

  public void setStartAreaId(Long startAreaId) {
    this.startAreaId = startAreaId;
  }

  public Long getPromocodeRedemptionId() {
    return promocodeRedemptionId;
  }

  public void setPromocodeRedemptionId(Long promocodeRedemptionId) {
    this.promocodeRedemptionId = promocodeRedemptionId;
  }

  public Long getAirportId() {
    return airportId;
  }

  public void setAirportId(Long airportId) {
    this.airportId = airportId;
  }

  public FareDetails getFareDetails() {
    return fareDetails;
  }

  public void setFareDetails(FareDetails fareDetails) {
    this.fareDetails = fareDetails;
  }

  @JsonProperty
  public Money getMinimumFare() {
    return fareDetails.getMinimumFare();
  }

  @JsonProperty
  public Money getBaseFare() {
    return fareDetails.getBaseFare();
  }

  @JsonProperty
  public Money getRatePerMile() {
    return fareDetails.getRatePerMile();
  }

  @JsonProperty
  public Money getRatePerMinute() {
    return fareDetails.getRatePerMinute();
  }

  @JsonProperty
  public Money getEstimatedFare() {
    return fareDetails.getEstimatedFare();
  }

  @JsonProperty
  public Money getBookingFee() {
    return fareDetails.getBookingFee();
  }

  @JsonProperty
  public Money getDistanceFare() {
    return fareDetails.getDistanceFare();
  }

  @JsonProperty
  public Money getTimeFare() {
    return fareDetails.getTimeFare();
  }

  @JsonProperty
  public Money getCityFee() {
    return fareDetails.getCityFee();
  }

  @JsonProperty
  public Money getCancellationFee() {
    return fareDetails.getCancellationFee();
  }

  @JsonProperty
  public Money getProcessingFee() {
    return fareDetails.getProcessingFee();
  }

  @JsonProperty
  public Money getSubTotal() {
    return fareDetails.getSubTotal();
  }

  @JsonProperty
  public Money getNormalFare() {
    return fareDetails.getNormalFare();
  }

  @JsonProperty
  public Money getSurgeFare() {
    return fareDetails.getSurgeFare();
  }

  @JsonProperty
  public Money getTotalFare() {
    return fareDetails.getTotalFare();
  }

  @JsonProperty
  public Money getFreeCreditCharged() {
    return fareDetails.getFreeCreditCharged();
  }

  @JsonProperty
  public Money getStripeCreditCharge() {
    return fareDetails.getStripeCreditCharge();
  }

  @JsonProperty
  public Money getDriverPayment() {
    return fareDetails.getDriverPayment();
  }

  @JsonProperty
  public Money getRaPayment() {
    return fareDetails.getRaPayment();
  }

  @JsonProperty
  public Money getTip() {
    return fareDetails.getTip();
  }

  @JsonProperty
  public Money getRoundUpAmount() {
    return fareDetails.getRoundUpAmount();
  }

  @JsonProperty
  public Money getAirportFee() {
    return fareDetails.getAirportFee();
  }

  @JsonProperty
  public Money getFareTotal() {
    return fareDetails.getFareTotal();
  }

  @JsonProperty
  public Money getTotalCharge() {
    return fareDetails.getTotalCharge();
  }

  @JsonProperty
  public Money getRideCost() {
    return fareDetails.getRideCost();
  }

  public String getApplePayToken() {
    return applePayToken;
  }

  public void setApplePayToken(String applePayToken) {
    this.applePayToken = applePayToken;
  }

  @JsonProperty
  public String getComment() {
    return comment;
  }

  public void setComment(String comment) {
    this.comment = comment;
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
