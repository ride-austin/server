package com.rideaustin.report.params;

import java.time.Instant;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class CompletedOnBeforeReportParams {

  private Instant completedOnBefore;

}
