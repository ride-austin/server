package com.rideaustin.service.model;

import static com.rideaustin.utils.SafeZeroUtils.safeZero;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.rideaustin.Constants;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ApiModel
public class RideReportEntry {

  @ApiModelProperty(required = true)
  private final Date date;
  @ApiModelProperty(required = true)
  private final Long ridesCount;
  @ApiModelProperty(required = true)
  private Long priorityFaresRidesCount = 0L;
  @ApiModelProperty(required = true)
  private BigDecimal distanceTraveled;
  @ApiModelProperty(required = true)
  private BigDecimal averageDistanceTraveled;
  @ApiModelProperty(required = true)
  private BigDecimal totalFares;
  @ApiModelProperty(required = true)
  private Double averageTotalFares;
  @ApiModelProperty(required = true)
  private Long cancelledRidesCount;

  public RideReportEntry(Date date, Long ridesCount) {
    this.date = date;
    this.ridesCount = ridesCount;
    this.distanceTraveled = BigDecimal.ZERO;
    this.totalFares = BigDecimal.ZERO;
    this.priorityFaresRidesCount = 0L;
    this.cancelledRidesCount = 0L;
  }

  public RideReportEntry(Date date, Long ridesCount, BigDecimal distanceTraveled, BigDecimal averageDistanceTraveled,
    BigDecimal totalFares, Double averageTotalFares, Long cancelledRidesCount) {
    this.date = date;
    this.ridesCount = safeZero(ridesCount);
    this.distanceTraveled = safeZero(distanceTraveled);
    this.averageDistanceTraveled = safeZero(averageDistanceTraveled);
    this.totalFares = safeZero(totalFares);
    this.averageTotalFares = safeZero(averageTotalFares);
    this.cancelledRidesCount = safeZero(cancelledRidesCount);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }

    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    RideReportEntry that = (RideReportEntry) o;

    return new EqualsBuilder()
      .append(date, that.date)
      .append(ridesCount, that.ridesCount)
      .append(priorityFaresRidesCount, that.priorityFaresRidesCount)
      .append(distanceTraveled, that.distanceTraveled)
      .append(averageDistanceTraveled, that.averageDistanceTraveled)
      .append(totalFares, that.totalFares)
      .append(averageTotalFares, that.averageTotalFares)
      .isEquals();
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder(17, 37)
      .append(date)
      .append(ridesCount)
      .append(priorityFaresRidesCount)
      .append(distanceTraveled)
      .append(averageDistanceTraveled)
      .append(totalFares)
      .append(averageTotalFares)
      .toHashCode();
  }

  public BigDecimal getDistanceTraveledInMiles() {
    return distanceTraveled == null ? null :
      Constants.MILES_PER_METER.multiply(distanceTraveled)
        .setScale(2, RoundingMode.HALF_UP);
  }

  public BigDecimal getAverageDistanceTraveledInMiles() {
    return averageDistanceTraveled == null ? null :
      Constants.MILES_PER_METER.multiply(averageDistanceTraveled)
        .setScale(2, RoundingMode.HALF_UP);
  }
}
