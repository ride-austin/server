package com.rideaustin.report;

import javax.inject.Inject;

import com.google.common.collect.ImmutableMap;
import com.rideaustin.repo.dsl.PromocodeRedemptionReportDslRepository;
import com.rideaustin.report.adapter.PromocodeRedemptionsCountReportAdapter;
import com.rideaustin.report.adapter.ReportAdapter;
import com.rideaustin.report.entry.PromocodeRedemptionsCountByDateReportEntry;
import com.rideaustin.report.entry.PromocodeRedemptionsCountByHourReportEntry;
import com.rideaustin.report.model.ReportComponent;
import com.rideaustin.report.model.ReportParameterType;
import com.rideaustin.report.enums.GroupByTimePeriod;
import com.rideaustin.report.params.PromocodeRedemptionsCountReportParams;

@ReportComponent(
  id = 16,
  name = "Promocode redemptions count report",
  header = "Redemptions count of {codeLiteral} promocode from {startDate} to {endDate}",
  parameters = {
    @ReportComponent.Param(
      label = "Start date",
      name = "startDate",
      type = ReportParameterType.DATETIME,
      required = true,
      order = 1
    ),
    @ReportComponent.Param(
      label = "End date",
      name = "endDate",
      type = ReportParameterType.DATETIME,
      required = true,
      order = 2
    ),
    @ReportComponent.Param(
      label = "Code literal",
      name = "codeLiteral",
      type = ReportParameterType.STRING,
      required = true,
      order = 3
    ),
    @ReportComponent.Param(
      label = "Group by",
      name = "groupByTimePeriod",
      type = ReportParameterType.ENUM,
      enumClass = GroupByTimePeriod.class,
      defaultValue = "DAY",
      required = true,
      order = 4
    )
  }
)
public class PromocodeRedemptionsCountReport extends BaseReport<PromocodeRedemptionsCountByDateReportEntry, PromocodeRedemptionsCountReportParams> {

  private PromocodeRedemptionReportDslRepository reportRepository;

  @Inject
  public PromocodeRedemptionsCountReport(PromocodeRedemptionReportDslRepository reportRepository) {
    this.reportRepository = reportRepository;
  }

  @Override
  protected ReportAdapter<PromocodeRedemptionsCountByDateReportEntry> createAdapter() {
    ImmutableMap<String, Object> context = ImmutableMap.of(
      "codeLiteral", parameters.getCodeLiteral(),
      "startDate", parameters.getStartDate(),
      "endDate", parameters.getEndDate()
    );
    Class<? extends PromocodeRedemptionsCountByDateReportEntry> entryClass =
      parameters.getGroupByTimePeriod() == GroupByTimePeriod.DAY
        ? PromocodeRedemptionsCountByDateReportEntry.class : PromocodeRedemptionsCountByHourReportEntry.class;
    return new PromocodeRedemptionsCountReportAdapter(entryClass, context);
  }

  @Override
  protected void doExecute() {
    this.resultsStream = reportRepository.groupRedemptionsByTimePeriod(parameters.getStartDate(),
      parameters.getEndDate(), parameters.getCodeLiteral(), parameters.getGroupByTimePeriod())
      .stream();
  }
}
