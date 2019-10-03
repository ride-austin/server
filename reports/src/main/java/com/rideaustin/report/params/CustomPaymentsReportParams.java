package com.rideaustin.report.params;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class CustomPaymentsReportParams {
  private Instant createdAfter;
  private Instant createdBefore;
  private Instant paymentDate;
  private Long cityId;
}
