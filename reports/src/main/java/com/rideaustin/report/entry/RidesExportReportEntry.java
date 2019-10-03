package com.rideaustin.report.entry;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

import org.joda.money.Money;

import com.querydsl.core.Tuple;
import com.rideaustin.report.ReportField;
import com.rideaustin.report.TupleConsumer;

import lombok.Getter;

@Getter
public class RidesExportReportEntry implements TupleConsumer {
  @ReportField(order = 1, name = "Ride ID")
  private final Long rideId;
  @ReportField(order = 2, name = "Rider ID")
  private final Long riderId;
  @ReportField(order = 3, name = "Driver ID")
  private final Long driverId;
  @ReportField(order = 4, name = "Active driver ID")
  private final Long activeDriverId;
  @ReportField(order = 5)
  private final String riderFullName;
  @ReportField(order = 6)
  private final String riderEmail;
  @ReportField(order = 7)
  private final String driverFullName;
  @ReportField(order = 8)
  private final String driverEmail;
  @ReportField(order = 9)
  private final Instant rideCreatedOn;
  @ReportField(order = 10, name = "Ride start location latitude")
  private final Double startLat;
  @ReportField(order = 11, name = "Ride start location longitude")
  private final Double startLong;
  @ReportField(order = 12)
  private final Instant rideCompletedOn;
  @ReportField(order = 13, name = "Ride end location latitude")
  private final Double endLat;
  @ReportField(order = 14, name = "Ride end location longitude")
  private final Double endLong;
  @ReportField(order = 15)
  private final BigDecimal distanceTravelled;
  @ReportField(order = 16)
  private final Money baseFare;
  @ReportField(order = 17)
  private final Money distanceFare;
  @ReportField(order = 18)
  private final Money timeFare;
  @ReportField(order = 19)
  private final Money subtotal;
  @ReportField(order = 20)
  private final Money bookingFee;
  @ReportField(order = 21)
  private final Money cityFee;
  @ReportField(order = 22)
  private final Money totalFare;
  @ReportField(order = 23)
  private final Money driverPayment;
  @ReportField(order = 24)
  private final Instant tipDate;
  @ReportField(order = 25)
  private final Money tipAmount;

  public RidesExportReportEntry(Tuple tuple) {
    int index = 0;
    this.rideId = getLong(tuple, index++);
    this.riderId = getLong(tuple, index++);
    this.driverId = getLong(tuple, index++);
    this.activeDriverId = getLong(tuple, index++);
    this.riderFullName = String.format("%s %s", getString(tuple, index++), getString(tuple, index++));
    this.riderEmail = getString(tuple, index++);
    this.driverFullName = String.format("%s %s", Optional.ofNullable(getString(tuple, index++)).orElse(""),
      Optional.ofNullable(getString(tuple, index++)).orElse(""));
    this.driverEmail = getString(tuple, index++);
    this.rideCreatedOn = getInstantFromTimestamp(tuple, index++);
    this.startLat = getDouble(tuple, index++);
    this.startLong = getDouble(tuple, index++);
    this.rideCompletedOn = getInstantFromTimestamp(tuple, index++);
    this.endLat = getDouble(tuple, index++);
    this.endLong = getDouble(tuple, index++);
    this.distanceTravelled = getBigDecimal(tuple, index++);
    this.baseFare = getMoney(tuple, index++);
    this.distanceFare = getMoney(tuple, index++);
    this.timeFare = getMoney(tuple, index++);
    this.subtotal = getMoney(tuple, index++);
    this.bookingFee = getMoney(tuple, index++);
    this.cityFee = getMoney(tuple, index++);
    this.totalFare = getMoney(tuple, index++);
    this.driverPayment = getMoney(tuple, index++);
    this.tipDate = getInstantFromTimestamp(tuple, index++);
    this.tipAmount = getMoney(tuple, index);
  }

}
