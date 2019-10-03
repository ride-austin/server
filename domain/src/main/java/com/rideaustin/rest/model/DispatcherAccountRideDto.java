package com.rideaustin.rest.model;

import java.util.Date;

import org.joda.money.Money;

import com.querydsl.core.annotations.QueryProjection;
import com.rideaustin.model.Address;
import com.rideaustin.model.enums.RideStatus;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@ApiModel
public class DispatcherAccountRideDto extends MobileRiderRideDto {

  @ApiModelProperty(required = true)
  private final DispatcherRiderDto passenger;

  @QueryProjection
  public DispatcherAccountRideDto(long id, Long riderId, RideStatus status, Date driverAcceptedOn, Date completedOn,
    Money tip, Double driverRating, Money estimatedFare, Double startLocationLat, Double startLocationLong, Double endLocationLat,
    Double endLocationLong, String startAddress, String endAddress, Address start, Address end, Money totalFare,
    Money driverPayment, String carType, String comment, Money freeCreditCharged, String mapUrl, Long cityId,
    Money roundUpAmount, Long activeDriverId, Long driverId, Double rating, Integer grantedDriverTypesBitmask,
    Long userId, String email, String firstname, String lastname, String phoneNumber, Boolean active, Long carId, String color,
    String license, String make, String model, String year, Integer carCategoriesBitmask, String riderFirstname,
    String riderLastName, String riderPhoneNumber) {
    super(id, riderId, status, driverAcceptedOn, completedOn, tip, driverRating, estimatedFare, startLocationLat, startLocationLong,
      endLocationLat, endLocationLong, startAddress, endAddress, start, end, totalFare, driverPayment, carType,
      comment, freeCreditCharged, mapUrl, cityId, roundUpAmount, activeDriverId, driverId, rating, grantedDriverTypesBitmask,
      userId, email, firstname, lastname, phoneNumber, active, carId, color, license, make, model, year, carCategoriesBitmask);
    this.passenger = new DispatcherRiderDto(riderFirstname, riderLastName, riderPhoneNumber);
  }

  @Getter
  @ApiModel
  @AllArgsConstructor
  private static class DispatcherRiderDto {
    @ApiModelProperty(required = true)
    private final String firstName;
    @ApiModelProperty(required = true)
    private final String lastName;
    @ApiModelProperty(required = true)
    private final String phoneNumber;
  }
}
