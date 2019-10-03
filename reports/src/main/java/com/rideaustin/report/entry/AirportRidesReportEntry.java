package com.rideaustin.report.entry;

import java.time.LocalDate;

import com.rideaustin.report.ReportField;
import com.rideaustin.report.FormatAs;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AirportRidesReportEntry {

  @ReportField(order = 1, format = FormatAs.DATE)
  private final LocalDate date;

  @ReportField(name = "# rides", order = 2, format = FormatAs.NUMERIC)
  private final Long count;

}
