package com.rideaustin.report.entry;

import java.time.Instant;

import com.querydsl.core.Tuple;
import com.rideaustin.model.enums.PayoneerStatus;
import com.rideaustin.report.FormatAs;
import com.rideaustin.report.ReportField;
import com.rideaustin.report.TupleConsumer;
import com.rideaustin.service.user.CarTypesUtils;

import lombok.Getter;

@Getter
public class DriversExportReportEntry implements TupleConsumer {

  @ReportField(order = 1, name = "Driver ID")
  private final Long driverId;
  @ReportField(order = 2)
  private final Instant driverSignupDate;
  @ReportField(order = 3)
  private final String firstName;
  @ReportField(order = 4)
  private final String lastName;
  @ReportField(order = 5)
  private final String email;
  @ReportField(order = 6)
  private final String phoneNumber;
  @ReportField(order = 7, format = FormatAs.YES_NO)
  private final Boolean active;
  @ReportField(order = 8)
  private final Instant activationDate;
  @ReportField(order = 9, format = FormatAs.YES_NO)
  private final Boolean enabled;
  @ReportField(order = 10)
  private final String carColor;
  @ReportField(order = 11)
  private final String licensePlate;
  @ReportField(order = 12)
  private final String carMake;
  @ReportField(order = 13)
  private final String carModel;
  @ReportField(order = 14)
  private final String carYear;
  @ReportField(order = 15)
  private final String carCategory;
  @ReportField(order = 16)
  private final PayoneerStatus payoneerStatus;

  public DriversExportReportEntry(Tuple tuple) {
    int index = 0;
    this.driverId = getLong(tuple, index++);
    this.driverSignupDate = getInstantFromTimestamp(tuple, index++);
    this.firstName = getString(tuple, index++);
    this.lastName = getString(tuple, index++);
    this.email = getString(tuple, index++);
    this.phoneNumber = getString(tuple, index++);
    this.active = getBoolean(tuple, index++);
    this.activationDate = getInstantFromDate(tuple, index++);
    this.enabled = getBoolean(tuple, index++);
    this.carColor = getString(tuple, index++);
    this.licensePlate = getString(tuple, index++);
    this.carMake = getString(tuple, index++);
    this.carModel = getString(tuple, index++);
    this.carYear = getString(tuple, index++);
    this.carCategory = String.join(", ", CarTypesUtils.fromBitMask(getInteger(tuple, index++)));
    this.payoneerStatus = get(tuple, index, PayoneerStatus.class);
  }

}
