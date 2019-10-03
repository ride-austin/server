package com.rideaustin.report;

import javax.inject.Inject;

import com.google.common.collect.ImmutableMap;
import com.rideaustin.repo.dsl.PromocodeReportDslRepository;
import com.rideaustin.report.adapter.DefaultReportAdapter;
import com.rideaustin.report.adapter.ReportAdapter;
import com.rideaustin.report.entry.PromocodeUsageReportEntry;
import com.rideaustin.report.model.ReportComponent;
import com.rideaustin.report.model.ReportParameterType;
import com.rideaustin.report.params.PromocodeUsageReportParams;

@ReportComponent(
  id = 13,
  name = "Promocode Usage Report",
  header = "All riders using promo code {codeLiteral}",
  parameters = {
    @ReportComponent.Param(
      label = "Code literal",
      name = "codeLiteral",
      type = ReportParameterType.STRING,
      required = true,
      order = 1
    ),
    @ReportComponent.Param(
      label = "Completed trips only",
      name = "completedOnly",
      description = "Include redemptions only for completed rides",
      type = ReportParameterType.BOOLEAN,
      required = true,
      defaultValue = "false",
      order = 2
    )
  }
)
public class PromocodeUsageReport extends BaseReport<PromocodeUsageReportEntry, PromocodeUsageReportParams> {

  private final PromocodeReportDslRepository reportRepository;

  @Inject
  public PromocodeUsageReport(PromocodeReportDslRepository reportRepository) {
    this.reportRepository = reportRepository;
  }

  @Override
  protected ReportAdapter<PromocodeUsageReportEntry> createAdapter() {
    return new DefaultReportAdapter<>(PromocodeUsageReportEntry.class, ImmutableMap.of(
      "codeLiteral", parameters.getCodeLiteral()
    ));
  }

  @Override
  protected void doExecute() {
    this.resultsStream = reportRepository.getPromocodeUsageReport(parameters.getCodeLiteral(),
      parameters.isCompletedOnly()).stream();
  }
}
