package com.rideaustin.report;

import java.util.Date;

import javax.inject.Inject;

import com.google.common.collect.ImmutableMap;
import com.rideaustin.repo.dsl.CampaignReportDslRepository;
import com.rideaustin.report.adapter.DefaultReportAdapter;
import com.rideaustin.report.adapter.ReportAdapter;
import com.rideaustin.report.entry.CampaignTripReportEntry;
import com.rideaustin.report.model.NestedReport;
import com.rideaustin.report.params.CampaignCompositeReportParams;

import lombok.RequiredArgsConstructor;

@NestedReport
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class CampaignTripReport extends BaseReport<CampaignTripReportEntry, CampaignCompositeReportParams> {

  private final CampaignReportDslRepository repository;

  @Override
  protected ReportAdapter<CampaignTripReportEntry> createAdapter() {
    return new DefaultReportAdapter<>(CampaignTripReportEntry.class,
      ImmutableMap.of("campaign", parameters.getCampaign(), "startDate", parameters.getStartDate(), "endDate", parameters.getEndDate()));
  }

  @Override
  protected void doExecute() {
    this.resultsStream = repository.getCampaignRides(parameters.getCampaign(), Date.from(parameters.getStartDate()),
      Date.from(parameters.getEndDate()))
      .stream();
  }
}
