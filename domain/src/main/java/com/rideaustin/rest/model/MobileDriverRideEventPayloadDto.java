package com.rideaustin.rest.model;

import java.math.BigDecimal;

import org.joda.money.Money;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.querydsl.core.annotations.QueryProjection;
import com.rideaustin.model.Address;
import com.rideaustin.model.enums.RideStatus;

import lombok.Getter;

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class MobileDriverRideEventPayloadDto extends MobileDriverRideDto {

  private final Money cancellationFee;

  @QueryProjection
  public MobileDriverRideEventPayloadDto(Long id, Long driverId, RideStatus status, Long riderId, String photoUrl,
    String firstname, String lastname, String phoneNumber, String email, double rating, Double startLocationLat,
    Double startLocationLong, Double endLocationLat, Double endLocationLong, String startAddress, String endAddress,
    Address start, Address end, BigDecimal surgeFactor, Money driverPayment, String title, String carCategory,
    String plainIconUrl, String configuration, Integer requestedDriverTypeBitmask, String comment, Money freeCreditCharged,
    String mapUrl, Money cancellationFee) {
    super(id, driverId, status, riderId, photoUrl, firstname, lastname, phoneNumber, email, rating, startLocationLat,
      startLocationLong, endLocationLat, endLocationLong, startAddress, endAddress, start, end, surgeFactor,
      driverPayment, title, carCategory, plainIconUrl, configuration, requestedDriverTypeBitmask, comment, freeCreditCharged, mapUrl);
    this.cancellationFee = cancellationFee;
  }
}
