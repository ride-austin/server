package com.rideaustin.test.response;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;
import java.util.Set;

import org.joda.money.Money;

import com.rideaustin.model.Address;
import com.rideaustin.model.enums.ActiveDriverStatus;
import com.rideaustin.model.enums.PaymentProvider;
import com.rideaustin.model.enums.PaymentStatus;
import com.rideaustin.model.enums.RideStatus;
import com.rideaustin.model.ride.Car;
import com.rideaustin.model.user.Driver;
import com.rideaustin.model.user.Rider;
import com.rideaustin.model.user.RiderCard;
import com.rideaustin.rest.model.CityCarTypeDto;
import com.rideaustin.rest.model.CityDriverTypeDto;
import com.rideaustin.rest.model.RideUpgradeRequestDto;
import com.rideaustin.service.location.model.LocationObject;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TestRideDto {

  private long id;
  private Date createdDate;
  private Date updatedDate;
  private TestActiveDriver activeDriver;
  private Rider rider;
  private RideStatus status;
  private PaymentStatus paymentStatus;
  private BigDecimal distanceTravelled;
  private BigDecimal distanceTravelledByGoogle;
  private Date startedOn;
  private Date driverReachedOn;
  private Date driverAcceptedOn;
  private Date completedOn;
  private Long estimatedTimeArrive;
  private Date cancelledOn;
  private BigDecimal surgeFactor = BigDecimal.ONE;
  private Double startLocationLat;
  private Double startLocationLong;
  private Double endLocationLat;
  private Double endLocationLong;
  private Long startAreaId;
  private Address start = new Address();
  private Address end = new Address();
  private String endAddress = "";
  private Double driverRating;
  private Double riderRating;
  private CityCarTypeDto requestedCarType;
  private CityDriverTypeDto requestedDriverType;

  private String trackingShareToken;
  private Date tippedOn;
  private Long cityId;
  private Long airportId;
  private String startAddress;
  private Money minimumFare;
  private Money baseFare;
  private Money ratePerMile;
  private Money ratePerMinute;
  private Money estimatedFare;
  private Money bookingFee;
  private Money distanceFare;
  private Money timeFare;
  private Money cityFee;
  private Money cancellationFee;
  private Money processingFee;
  private Money subTotal;
  private Money normalFare;
  private Money surgeFare;
  private Money totalFare;
  private Money freeCreditCharged;
  private Money stripeCreditCharge;
  private Money driverPayment;
  private Money raPayment;
  private Money totalCharge;
  private Money fareTotal;
  private Money rideCost;
  private Money tip;
  private Money roundUpAmount;
  private Money airportFee;
  private Money upfrontCharge;
  private RiderCard riderCard;
  private String applePayToken;
  private PaymentProvider paymentProvider;
  private Date freeCancellationExpiresOn;
  private Date estimatedTimeCompletion;
  private boolean tippingAllowed;
  private Date tipUntil;
  private RideUpgradeRequestDto upgradeRequest;

  @Override
  public String toString() {
    return "TestRideDto{" +
      "id=" + id +
      ", createdDate=" + createdDate +
      ", updatedDate=" + updatedDate +
      ", activeDriver=" + activeDriver +
      ", rider=" + rider +
      ", status=" + status +
      ", paymentStatus=" + paymentStatus +
      ", distanceTravelled=" + distanceTravelled +
      ", distanceTravelledByGoogle=" + distanceTravelledByGoogle +
      ", startedOn=" + startedOn +
      ", driverReachedOn=" + driverReachedOn +
      ", driverAcceptedOn=" + driverAcceptedOn +
      ", completedOn=" + completedOn +
      ", estimatedTimeArrive=" + estimatedTimeArrive +
      ", cancelledOn=" + cancelledOn +
      ", surgeFactor=" + surgeFactor +
      ", startLocationLat=" + startLocationLat +
      ", startLocationLong=" + startLocationLong +
      ", endLocationLat=" + endLocationLat +
      ", endLocationLong=" + endLocationLong +
      ", startAreaId=" + startAreaId +
      ", start=" + start +
      ", end=" + end +
      ", endAddress='" + endAddress + '\'' +
      ", driverRating=" + driverRating +
      ", riderRating=" + riderRating +
      ", requestedCarType=" + requestedCarType +
      ", requestedDriverType=" + requestedDriverType +
      ", trackingShareToken='" + trackingShareToken + '\'' +
      ", tippedOn=" + tippedOn +
      ", cityId=" + cityId +
      ", airportId=" + airportId +
      ", startAddress='" + startAddress + '\'' +
      ", minimumFare=" + minimumFare +
      ", baseFare=" + baseFare +
      ", ratePerMile=" + ratePerMile +
      ", ratePerMinute=" + ratePerMinute +
      ", estimatedFare=" + estimatedFare +
      ", bookingFee=" + bookingFee +
      ", distanceFare=" + distanceFare +
      ", timeFare=" + timeFare +
      ", cityFee=" + cityFee +
      ", cancellationFee=" + cancellationFee +
      ", processingFee=" + processingFee +
      ", subTotal=" + subTotal +
      ", normalFare=" + normalFare +
      ", surgeFare=" + surgeFare +
      ", totalFare=" + totalFare +
      ", freeCreditCharged=" + freeCreditCharged +
      ", stripeCreditCharge=" + stripeCreditCharge +
      ", driverPayment=" + driverPayment +
      ", raPayment=" + raPayment +
      ", totalCharge=" + totalCharge +
      ", fareTotal=" + fareTotal +
      ", rideCost=" + rideCost +
      ", tip=" + tip +
      ", roundUpAmount=" + roundUpAmount +
      ", airportFee=" + airportFee +
      ", riderCard=" + riderCard +
      ", applePayToken='" + applePayToken + '\'' +
      ", paymentProvider=" + paymentProvider +
      ", freeCancellationExpiresOn=" + freeCancellationExpiresOn +
      ", estimatedTimeCompletion=" + estimatedTimeCompletion +
      ", tippingAllowed=" + tippingAllowed +
      ", tipUntil=" + tipUntil +
      ", upgradeRequest=" + upgradeRequest +
      '}';
  }

  @Getter
  @Setter
  public static class TestLocationObject {

    private String type;
    private long ownerId;
    private double latitude;
    private double longitude;
    private double heading;
    private double speed;
    private double course;
    private String status;
    private String parameter1;
    private String parameter2;
    private Date locationUpdateDate;

    @Override
    public String toString() {
      return "TestLocationObject{" +
        "type='" + type + '\'' +
        ", ownerId=" + ownerId +
        ", latitude=" + latitude +
        ", longitude=" + longitude +
        ", heading=" + heading +
        ", speed=" + speed +
        ", course=" + course +
        ", status='" + status + '\'' +
        ", availableCarCategories='" + parameter1 + '\'' +
        ", availableDriverTypes='" + parameter2 + '\'' +
        ", locationUpdateDate=" + locationUpdateDate +
        '}';
    }
  }

  @Getter
  @Setter
  public static class TestActiveDriver {
    private long id;
    private Date createdDate;
    private Date updatedDate;
    private ActiveDriverStatus status;
    private LocationObject locationObject;
    private Driver driver;
    private Car selectedCar;
    private Date inactiveOn;
    private int availableCarCategoriesBitmask;
    private Integer availableDriverTypesBitmask;
    private Map<String, Integer> consecutiveDeclinedRequests;
    private Long cityId;
    private Double directDistanceToRider;
    private Long drivingTimeToRider;
    private Long drivingDistanceToRider;
    private Double latitude;
    private Double longitude;
    private Double speed;
    private Double heading;
    private Double course;
    private Set<String> carCategories;
    private Set<String> driverTypes;
    private Long locationUpdatedOn;

    @Override
    public String toString() {
      return "TestActiveDriver{" +
        "id=" + id +
        ", createdDate=" + createdDate +
        ", updatedDate=" + updatedDate +
        ", status=" + status +
        ", locationObject=" + locationObject +
        ", driver=" + driver +
        ", selectedCar=" + selectedCar +
        ", inactiveOn=" + inactiveOn +
        ", availableCarCategoriesBitmask=" + availableCarCategoriesBitmask +
        ", availableDriverTypesBitmask=" + availableDriverTypesBitmask +
        ", consecutiveDeclinedRequests=" + consecutiveDeclinedRequests +
        ", cityId=" + cityId +
        ", directDistanceToRider=" + directDistanceToRider +
        ", drivingTimeToRider=" + drivingTimeToRider +
        ", drivingDistanceToRider=" + drivingDistanceToRider +
        ", latitude=" + latitude +
        ", longitude=" + longitude +
        ", speed=" + speed +
        ", heading=" + heading +
        ", course=" + course +
        ", carCategories=" + carCategories +
        ", driverTypes=" + driverTypes +
        ", locationUpdatedOn=" + locationUpdatedOn +
        '}';
    }
  }
}
