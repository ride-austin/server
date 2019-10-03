package com.rideaustin.report;

import java.util.Date;

import javax.inject.Inject;

import com.google.common.collect.ImmutableMap;
import com.rideaustin.repo.dsl.CampaignReportDslRepository;
import com.rideaustin.report.adapter.DefaultReportAdapter;
import com.rideaustin.report.adapter.ReportAdapter;
import com.rideaustin.report.entry.CampaignStatsReportEntry;
import com.rideaustin.report.model.NestedReport;
import com.rideaustin.report.params.CampaignCompositeReportParams;

import lombok.RequiredArgsConstructor;

@NestedReport
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class CampaignStatsReport extends BaseReport<CampaignStatsReportEntry, CampaignCompositeReportParams> {

  private final CampaignReportDslRepository repository;

  @Override
  protected ReportAdapter<CampaignStatsReportEntry> createAdapter() {
    return new DefaultReportAdapter<>(CampaignStatsReportEntry.class,
      ImmutableMap.of("campaign", parameters.getCampaign(), "startDate", parameters.getStartDate(),
        "endDate", parameters.getEndDate()));
  }

  @Override
  protected void doExecute() {
    this.resultsStream = repository.getCampaignStats(parameters.getCampaign(), Date.from(parameters.getStartDate()),
      Date.from(parameters.getEndDate())).stream();
  }
}
