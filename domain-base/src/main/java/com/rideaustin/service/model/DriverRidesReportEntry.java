package com.rideaustin.service.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

import com.rideaustin.Constants;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;

@Getter
@ApiModel
public class DriverRidesReportEntry {

  @ApiModelProperty(required = true)
  private final Long driverId;
  @ApiModelProperty(required = true)
  private final Long userId;
  @ApiModelProperty(required = true)
  private final String firstName;
  @ApiModelProperty(required = true)
  private final String lastName;
  @ApiModelProperty(required = true)
  private final Long completedRides;
  @ApiModelProperty(required = true)
  private final Long priorityFareRides;
  @ApiModelProperty(required = true)
  private final BigDecimal distanceTravelled;
  @ApiModelProperty(required = true)
  private final BigDecimal driverBasePayment;
  @ApiModelProperty(required = true)
  private final BigDecimal tips;
  @ApiModelProperty(required = true)
  private final BigDecimal priorityFare;
  @ApiModelProperty(required = true)
  private final BigDecimal cancellationFee;
  @ApiModelProperty(required = true)
  private final BigDecimal driverPayment;
  @ApiModelProperty(required = true)
  private final BigDecimal raGrossMargin;
  @ApiModelProperty(required = true)
  private final BigDecimal totalFare;

  public DriverRidesReportEntry() {
    this(null, null, null, null, null, null, null, null, null, null, null, null, null, null);
  }

  public DriverRidesReportEntry(Long driverId, Long userId, String firstName, String lastName,
    Long completedRides, Long priorityFareRides, BigDecimal distanceTravelled, BigDecimal driverBasePayment,
    BigDecimal tips, BigDecimal priorityFare, BigDecimal cancellationFee, BigDecimal driverPayment,
    BigDecimal raGrossMargin, BigDecimal totalFare) {
    this.firstName = firstName;
    this.lastName = lastName;
    this.userId = userId;
    this.driverId = driverId;
    this.completedRides = completedRides;
    this.priorityFareRides = priorityFareRides;
    this.distanceTravelled = distanceTravelled;
    this.driverBasePayment = driverBasePayment;
    this.tips = tips;
    this.priorityFare = priorityFare;
    this.cancellationFee = cancellationFee;
    this.driverPayment = driverPayment;
    this.raGrossMargin = raGrossMargin;
    this.totalFare = totalFare;
  }

  @ApiModelProperty(required = true)
  public BigDecimal getDistanceTraveledInMiles() {
    return distanceTravelled == null ? null :
      Constants.MILES_PER_METER.multiply(distanceTravelled)
        .setScale(2, RoundingMode.HALF_UP);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof DriverRidesReportEntry)) {
      return false;
    }
    DriverRidesReportEntry that = (DriverRidesReportEntry) o;
    return Objects.equals(firstName, that.firstName) &&
      Objects.equals(lastName, that.lastName) &&
      Objects.equals(userId, that.userId) &&
      Objects.equals(driverId, that.driverId) &&
      Objects.equals(completedRides, that.completedRides) &&
      Objects.equals(priorityFareRides, that.priorityFareRides) &&
      Objects.equals(distanceTravelled, that.distanceTravelled) &&
      Objects.equals(driverBasePayment, that.driverBasePayment) &&
      Objects.equals(tips, that.tips) &&
      Objects.equals(priorityFare, that.priorityFare) &&
      Objects.equals(cancellationFee, that.cancellationFee) &&
      Objects.equals(driverPayment, that.driverPayment) &&
      Objects.equals(raGrossMargin, that.raGrossMargin) &&
      Objects.equals(totalFare, that.totalFare);
  }

  @Override
  public int hashCode() {
    return Objects.hash(firstName, lastName, userId, driverId, completedRides, priorityFareRides, distanceTravelled,
      driverBasePayment, tips, priorityFare, cancellationFee, driverPayment, raGrossMargin, totalFare);
  }
}
