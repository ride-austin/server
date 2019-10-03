package com.rideaustin.report;

import java.time.LocalDate;
import java.util.List;

import javax.inject.Inject;

import com.rideaustin.model.reports.AvatarTripCountReportResultEntry;
import com.rideaustin.repo.dsl.RideReportDslRepository;
import com.rideaustin.report.adapter.DefaultReportAdapter;
import com.rideaustin.report.adapter.ReportAdapter;
import com.rideaustin.report.entry.RiderTripCountReportEntry;
import com.rideaustin.report.model.ReportComponent;
import com.rideaustin.report.model.ReportParameterType;
import com.rideaustin.report.params.RiderTripCountReportParams;

@ReportComponent(
  id = 15,
  name = "All riders - trip counts by week",
  header = "All riders - with trips completed through {endDate}",
  archive = true,
  parameters = {
    @ReportComponent.Param(
      label = "Completed on before",
      name = "completedOnBefore",
      type = ReportParameterType.DATETIME,
      required = true,
      order = 1
    )
  }
)
public class RiderTripCountReport extends AvatarTripCountReport<RiderTripCountReportEntry, RiderTripCountReportParams> {


  @Inject
  public RiderTripCountReport(RideReportDslRepository reportRepository) {
    super(reportRepository);
  }

  @Override
  protected RiderTripCountReportEntry createEntry(Long avatarId, LocalDate endOfWeek, String firstName, String lastName, String email, Long tripCountPerWeek) {
    return new RiderTripCountReportEntry(avatarId, endOfWeek, firstName, lastName, email, tripCountPerWeek);
  }

  @Override
  protected List<AvatarTripCountReportResultEntry> getRawData() {
    return reportRepository.getRiderTripsRaw(parameters.getCompletedOnBefore());
  }

  @Override
  protected ReportAdapter<RiderTripCountReportEntry> createAdapter() {
    return new DefaultReportAdapter<>(RiderTripCountReportEntry.class, createContext());
  }


}
