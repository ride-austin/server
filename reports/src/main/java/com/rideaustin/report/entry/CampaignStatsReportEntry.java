package com.rideaustin.report.entry;

import com.querydsl.core.annotations.QueryProjection;
import com.rideaustin.model.enums.RideStatus;
import com.rideaustin.report.ReportField;

import lombok.Getter;

@Getter
public class CampaignStatsReportEntry {

  @ReportField(order = 1, name = "Ride status")
  private final RideStatus status;
  @ReportField(order = 2, name = "# trips")
  private final long count;

  @QueryProjection
  public CampaignStatsReportEntry(RideStatus status, long count) {
    this.status = status;
    this.count = count;
  }
}
