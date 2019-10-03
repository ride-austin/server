package com.rideaustin.report;

import java.time.Instant;

import javax.inject.Inject;

import com.google.common.collect.ImmutableMap;
import com.rideaustin.repo.dsl.RideReportDslRepository;
import com.rideaustin.report.adapter.DefaultReportAdapter;
import com.rideaustin.report.adapter.ReportAdapter;
import com.rideaustin.report.entry.TNCDriverStatsReportEntry;
import com.rideaustin.report.model.NestedReport;
import com.rideaustin.report.params.TNCCompositeReportParams;

@NestedReport
public class TNCDriverStatsReport extends BaseReport<TNCDriverStatsReportEntry, TNCCompositeReportParams> {

  private RideReportDslRepository rideReportDslRepository;

  @Inject
  public TNCDriverStatsReport(RideReportDslRepository rideReportDslRepository) {
    this.rideReportDslRepository = rideReportDslRepository;
  }

  @Override
  protected ReportAdapter<TNCDriverStatsReportEntry> createAdapter() {
    return new DefaultReportAdapter<>(TNCDriverStatsReportEntry.class, ImmutableMap.of(
      "startDate", parameters.getStartDate(),
      "endDate", parameters.getEndDate()
    ));
  }

  @Override
  protected void doExecute() {
    Instant startDate = parameters.getStartDate();
    Instant endDate = parameters.getEndDate();
    this.resultsStream = rideReportDslRepository.getDriversStatsReport(startDate, endDate).stream();
  }
}
