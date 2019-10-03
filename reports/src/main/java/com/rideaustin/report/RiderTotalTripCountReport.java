package com.rideaustin.report;

import javax.inject.Inject;

import com.google.common.collect.ImmutableMap;
import com.rideaustin.Constants;
import com.rideaustin.repo.dsl.RideReportDslRepository;
import com.rideaustin.report.adapter.DefaultReportAdapter;
import com.rideaustin.report.adapter.ReportAdapter;
import com.rideaustin.report.entry.RiderTotalTripCountReportEntry;
import com.rideaustin.report.model.ReportComponent;
import com.rideaustin.report.model.ReportParameterType;
import com.rideaustin.report.params.RiderTotalTripCountReportParams;

@ReportComponent(
  id = 17,
  archive = true,
  name = "All riders - total trip count",
  header = "All riders - total trip counts completed through {endDate}",
  parameters = {
    @ReportComponent.Param(
      label = "Completed on before",
      name = "completedOnBefore",
      type = ReportParameterType.DATETIME,
      order = 1,
      required = true
    )
  }
)
public class RiderTotalTripCountReport extends BaseReport<RiderTotalTripCountReportEntry, RiderTotalTripCountReportParams> {

  private final RideReportDslRepository reportRepository;

  @Inject
  public RiderTotalTripCountReport(RideReportDslRepository reportRepository) {
    this.reportRepository = reportRepository;
  }

  @Override
  protected ReportAdapter<RiderTotalTripCountReportEntry> createAdapter() {
    return new DefaultReportAdapter<>(RiderTotalTripCountReportEntry.class, ImmutableMap.of(
      "endDate", Constants.DATETIME_FORMATTER.format(parameters.getCompletedOnBefore())
    ));
  }

  @Override
  protected void doExecute() {
    this.resultsStream = reportRepository.getRiderTripTotalCounts(parameters.getCompletedOnBefore()).stream();
  }
}
