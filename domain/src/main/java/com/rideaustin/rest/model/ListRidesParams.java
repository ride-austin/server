package com.rideaustin.rest.model;

import java.time.Instant;
import java.util.Date;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.springframework.format.annotation.DateTimeFormat;

import com.querydsl.core.BooleanBuilder;
import com.rideaustin.model.enums.RideStatus;
import com.rideaustin.model.ride.QRide;
import com.rideaustin.model.user.QDriver;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ApiModel
public class ListRidesParams {
  @ApiModelProperty("List of ride statuses to be included")
  private List<RideStatus> status;
  @ApiModelProperty("Rider email")
  private String riderEmail;
  @ApiModelProperty("Driver email")
  private String driverEmail;
  @ApiModelProperty("Zip code")
  private String zipCode;
  @ApiModelProperty(value = "Rider ID", example = "1")
  private Long riderId;
  @ApiModelProperty(value = "Driver ID", example = "1")
  private Long driverId;
  @ApiModelProperty(value = "City ID", example = "1")
  private Long cityId;
  @ApiModelProperty("Show charged rides only")
  private boolean charged = false;
  @ApiModelProperty("Rides created after")
  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
  private Instant createdOnAfter;
  @ApiModelProperty("Rides created before")
  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
  private Instant createdOnBefore;
  @ApiModelProperty("Rides completed after")
  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
  private Instant completedOnAfter;
  @ApiModelProperty("Rides completed before")
  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
  private Instant completedOnBefore;
  @ApiModelProperty("Rides cancelled after")
  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
  private Instant cancelledOnAfter;
  @ApiModelProperty("Rides cancelled before")
  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
  private Instant cancelledOnBefore;
  @ApiModelProperty("Rider phone number")
  private String phoneNumber;

  public void fill(BooleanBuilder bb) {
    QRide qRide = QRide.ride;
    if (CollectionUtils.isNotEmpty(status)) {
      bb.and(qRide.status.in(status));
    }
    if (!StringUtils.isEmpty(riderEmail)) {
      bb.and(qRide.rider.user.email.containsIgnoreCase(riderEmail));
    }
    if (!StringUtils.isEmpty(driverEmail)) {
      bb.and(qRide.activeDriver.driver.user.email.containsIgnoreCase(driverEmail));
    }
    if (charged) {
      bb.and(qRide.fareDetails.totalFare.isNotNull());
    }

    if (riderId != null) {
      bb.and(qRide.rider.id.eq(riderId));
    }
    if (driverId != null) {
      bb.and(QDriver.driver.id.eq(driverId));
    }
    if (cityId != null) {
      bb.and(qRide.cityId.eq(cityId));
    }
    if (createdOnAfter != null) {
      bb.and(qRide.createdDate.goe(Date.from(createdOnAfter)));
    }
    if (createdOnBefore != null) {
      bb.and(qRide.createdDate.loe(Date.from(createdOnBefore)));
    }
    if (completedOnAfter != null) {
      bb.and(qRide.completedOn.goe(Date.from(completedOnAfter)));
    }
    if (completedOnBefore != null) {
      bb.and(qRide.completedOn.loe(Date.from(completedOnBefore)));
    }
    if (zipCode != null) {
      bb.and(qRide.start.zipCode.eq(zipCode)
        .or(qRide.end.zipCode.eq(zipCode)));
    }
    if (cancelledOnAfter != null) {
      bb.and(qRide.cancelledOn.goe(Date.from(cancelledOnAfter)));
    }
    if (cancelledOnBefore != null) {
      bb.and(qRide.cancelledOn.loe(Date.from(cancelledOnBefore)));
    }

    if (!StringUtils.isEmpty(phoneNumber)) {
      bb.and(qRide.rider.user.phoneNumber.contains(phoneNumber)
        .or(qRide.activeDriver.driver.user.phoneNumber.contains(phoneNumber)));
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }

    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    ListRidesParams params = (ListRidesParams) o;

    return new EqualsBuilder()
      .append(status, params.status)
      .append(riderEmail, params.riderEmail)
      .append(driverEmail, params.driverEmail)
      .append(riderId, params.riderId)
      .append(driverId, params.driverId)
      .append(createdOnAfter, params.createdOnAfter)
      .append(createdOnBefore, params.createdOnBefore)
      .append(completedOnAfter, params.completedOnAfter)
      .append(completedOnBefore, params.completedOnBefore)
      .append(phoneNumber, params.phoneNumber)
      .isEquals();
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder(17, 37)
      .append(status)
      .append(riderEmail)
      .append(driverEmail)
      .append(riderId)
      .append(driverId)
      .append(createdOnAfter)
      .append(createdOnBefore)
      .append(completedOnAfter)
      .append(completedOnBefore)
      .append(phoneNumber)
      .toHashCode();
  }

}
