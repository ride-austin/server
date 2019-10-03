package com.rideaustin.report;

import java.util.Date;

import javax.inject.Inject;

import com.google.common.collect.ImmutableMap;
import com.rideaustin.repo.dsl.RiderReportDslRepository;
import com.rideaustin.report.adapter.DefaultReportAdapter;
import com.rideaustin.report.adapter.ReportAdapter;
import com.rideaustin.report.entry.RidersExportReportEntry;
import com.rideaustin.report.model.ReportComponent;
import com.rideaustin.report.model.ReportParameterType;
import com.rideaustin.report.params.RidersExportReportParams;

@ReportComponent(
  id = 8,
  name = "Riders Export report",
  description = "Riders Export report",
  header = "Riders export for {reportDate}",
  archive = true,
  upload = true,
  parameters = {
    @ReportComponent.Param(
      label = "Name",
      name = "name",
      type = ReportParameterType.STRING,
      order = 1
    ),
    @ReportComponent.Param(
      label = "Email",
      name = "email",
      type = ReportParameterType.STRING,
      order = 2
    ),
    @ReportComponent.Param(
      label = "Active",
      name = "active",
      type = ReportParameterType.BOOLEAN,
      order = 3
    ),
    @ReportComponent.Param(
      label = "Enabled",
      name = "enabled",
      type = ReportParameterType.BOOLEAN,
      order = 4
    ),
    @ReportComponent.Param(
      label = "Rider ID",
      name = "riderId",
      type = ReportParameterType.INTEGER,
      order = 5
    )
  }
)
public class RidersExportReport extends BaseReport<RidersExportReportEntry, RidersExportReportParams> {

  private RiderReportDslRepository riderReportDslRepository;

  @Inject
  public RidersExportReport(RiderReportDslRepository riderReportDslRepository) {
    this.riderReportDslRepository = riderReportDslRepository;
  }

  @Override
  protected ReportAdapter<RidersExportReportEntry> createAdapter() {
    return new DefaultReportAdapter<>(RidersExportReportEntry.class,
      ImmutableMap.of("reportDate", Date.from(DefaultDateValues.CURRENT_DATE.getValue())));
  }

  @Override
  protected void doExecute() {
    this.resultsStream = riderReportDslRepository.exportRiders(parameters.asListRidersParams()).stream();
  }
}
