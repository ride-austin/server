package com.rideaustin.report;

import java.time.LocalDate;
import java.util.List;

import javax.inject.Inject;

import com.rideaustin.model.reports.AvatarTripCountReportResultEntry;
import com.rideaustin.repo.dsl.RideReportDslRepository;
import com.rideaustin.report.adapter.DefaultReportAdapter;
import com.rideaustin.report.adapter.ReportAdapter;
import com.rideaustin.report.entry.DriverTripCountReportEntry;
import com.rideaustin.report.model.ReportComponent;
import com.rideaustin.report.model.ReportParameterType;
import com.rideaustin.report.params.DriverTripCountReportParams;

@ReportComponent(
  id = 14,
  name = "All drivers - trip counts by week",
  header = "All drivers - with trips completed through {endDate}",
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
public class DriverTripCountReport extends AvatarTripCountReport<DriverTripCountReportEntry, DriverTripCountReportParams> {

  @Inject
  public DriverTripCountReport(RideReportDslRepository reportRepository) {
    super(reportRepository);
  }

  @Override
  protected DriverTripCountReportEntry createEntry(Long avatarId, LocalDate endOfWeek, String firstName, String lastName, String email, Long tripCountPerWeek) {
    return new DriverTripCountReportEntry(avatarId, endOfWeek, firstName, lastName, email, tripCountPerWeek);
  }

  @Override
  protected List<AvatarTripCountReportResultEntry> getRawData() {
    return reportRepository.getDriverTripsRaw(parameters.getCompletedOnBefore());
  }

  @Override
  protected ReportAdapter<DriverTripCountReportEntry> createAdapter() {
    return new DefaultReportAdapter<>(DriverTripCountReportEntry.class, createContext());
  }

}
