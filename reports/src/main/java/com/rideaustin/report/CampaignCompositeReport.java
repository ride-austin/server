package com.rideaustin.report;

import javax.inject.Inject;

import com.google.common.collect.ImmutableMap;
import com.rideaustin.repo.dsl.CampaignDslRepository;
import com.rideaustin.report.adapter.DefaultReportAdapter;
import com.rideaustin.report.adapter.ReportAdapter;
import com.rideaustin.report.entry.CompositeReportEntry;
import com.rideaustin.report.model.ReportComponent;
import com.rideaustin.report.model.ReportFormat;
import com.rideaustin.report.params.CampaignCompositeReportParams;
import com.rideaustin.report.params.CampaignCompositeReportParamsProvider;

@ReportComponent(
  id = 18,
  name = "Campaign trips report",
  description = "Campaign trips report",
  header = "Trips report for {campaign}",
  format = ReportFormat.XLSX,
  parametersProvider = CampaignCompositeReportParamsProvider.class
)
public class CampaignCompositeReport extends BaseCompositeReport<CampaignCompositeReportParams> {

  private final CampaignDslRepository repository;

  @Inject
  private CampaignCompositeReport(CampaignTripReport campaignTripReport, CampaignStatsReport campaignStatsReport, CampaignDslRepository repository) {
    super(campaignTripReport, campaignStatsReport);
    this.repository = repository;
  }

  @Override
  protected ReportAdapter<CompositeReportEntry> createAdapter() {
    return new DefaultReportAdapter<>(CompositeReportEntry.class,
      ImmutableMap.of("campaign", repository.findOne(parameters.getCampaign()).getName(),
        "startDate", parameters.getStartDate(), "endDate", parameters.getEndDate()));
  }
}
