package com.rideaustin.report.entry;

import java.math.BigDecimal;

import com.querydsl.core.Tuple;
import com.rideaustin.Constants;
import com.rideaustin.report.ReportField;
import com.rideaustin.report.TupleConsumer;

import lombok.Getter;

@Getter
public class TNCDriverStatsReportEntry implements TupleConsumer {

  @ReportField(order = 1, name = "Driver ID")
  private final Long driverId;
  @ReportField(order = 2)
  private final String firstName;
  @ReportField(order = 3)
  private final String lastName;
  @ReportField(order = 4)
  private final BigDecimal totalHoursDriven;
  @ReportField(order = 5)
  private final BigDecimal totalMilesDriven;

  public TNCDriverStatsReportEntry(Tuple tuple) {
    int index = 0;
    driverId = getLong(tuple, index++);
    firstName = getString(tuple, index++);
    lastName = getString(tuple, index++);
    totalHoursDriven = getBigDecimal(tuple, index++).divide(Constants.SECONDS_PER_HOUR, 2, Constants.ROUNDING_MODE);
    totalMilesDriven = getBigDecimal(tuple, index).multiply(Constants.MILES_PER_METER);
  }

}
