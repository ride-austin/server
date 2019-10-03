package com.rideaustin.report.params;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class TripFulfillmentReportParams {

  private Instant startDateTime;

  private Instant endDateTime;

  private Long interval;

}
