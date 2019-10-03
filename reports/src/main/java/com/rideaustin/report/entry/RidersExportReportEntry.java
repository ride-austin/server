package com.rideaustin.report.entry;

import com.querydsl.core.Tuple;
import com.rideaustin.report.FormatAs;
import com.rideaustin.report.ReportField;
import com.rideaustin.report.TupleConsumer;

import lombok.Getter;

@Getter
public class RidersExportReportEntry implements TupleConsumer {

  @ReportField(order = 1, name = "Rider ID")
  private final Long riderId;
  @ReportField(order = 2)
  private final String email;
  @ReportField(order = 3)
  private final String phoneNumber;
  @ReportField(order = 4)
  private final String lastName;
  @ReportField(order = 5)
  private final String firstName;
  @ReportField(order = 6, format = FormatAs.YES_NO)
  private final Boolean active;
  @ReportField(order = 7, format = FormatAs.YES_NO)
  private final Boolean enabled;

  public RidersExportReportEntry(Tuple tuple) {
    int index = 0;
    this.riderId = getLong(tuple, index++);
    this.email = getString(tuple, index++);
    this.phoneNumber = getString(tuple, index++);
    this.lastName = getString(tuple, index++);
    this.firstName = getString(tuple, index++);
    this.active = getBoolean(tuple, index++);
    this.enabled = getBoolean(tuple, index);
  }

}
