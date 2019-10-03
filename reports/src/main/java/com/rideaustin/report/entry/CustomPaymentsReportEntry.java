package com.rideaustin.report.entry;

import java.time.Instant;

import org.joda.money.Money;

import com.rideaustin.report.FormatAs;
import com.rideaustin.report.ReportField;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CustomPaymentsReportEntry {

  @ReportField(order = 1, name = "Driver payoneer ID")
  private final String payoneerId;
  @ReportField(order = 2)
  private final Money amount;
  @ReportField(order = 3, name = "Payment ID")
  private final Long paymentId;
  @ReportField(order = 4)
  private final String currency;
  @ReportField(order = 5)
  private final String description;
  @ReportField(order = 6, format = FormatAs.DATE)
  private final Instant paymentDate;

}
