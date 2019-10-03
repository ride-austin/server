package com.rideaustin.report.entry;

import java.time.Instant;

import com.querydsl.core.Tuple;
import com.rideaustin.report.FormatAs;
import com.rideaustin.report.ReportField;
import com.rideaustin.report.TupleConsumer;

import lombok.Getter;

@Getter
public class FingerprintStatusReportEntry implements TupleConsumer {

  @ReportField(name = "Driver ID", order = 1)
  private final long driverId;
  @ReportField(order = 2)
  private final String firstName;
  @ReportField(order = 3)
  private final String middleName;
  @ReportField(order = 4)
  private final String lastName;
  @ReportField(order = 5)
  private final String email;
  @ReportField(order = 6, format = FormatAs.DATE)
  private final Instant dateOfBirth;
  @ReportField(order = 7, name = "Fingerprint cleared?", format = FormatAs.YES_NO)
  private final boolean fingerprintCleared;

  public FingerprintStatusReportEntry(Tuple tuple) {
    int index = 0;
    this.driverId = getLong(tuple, index++);
    this.firstName = getString(tuple, index++);
    this.middleName = getString(tuple, index++);
    this.lastName = getString(tuple, index++);
    this.email = getString(tuple, index++);
    this.dateOfBirth = getInstantFromDate(tuple, index++);
    this.fingerprintCleared = getBoolean(tuple, index);
  }

}
