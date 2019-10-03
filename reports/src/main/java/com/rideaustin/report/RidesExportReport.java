package com.rideaustin.report;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import javax.inject.Inject;

import com.rideaustin.model.enums.RideStatus;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.rideaustin.Constants;
import com.rideaustin.repo.dsl.RideReportDslRepository;
import com.rideaustin.report.adapter.DefaultReportAdapter;
import com.rideaustin.report.adapter.ReportAdapter;
import com.rideaustin.report.entry.RidesExportReportEntry;
import com.rideaustin.report.model.ReportComponent;
import com.rideaustin.report.model.ReportParameterType;
import com.rideaustin.report.params.RidesExportReportParams;

@ReportComponent(
  id = 6,
  archive = true,
  name = "Rides Export report",
  description = "Rides Export report",
  header = "Rides export for {period}",
  parameters = {
    @ReportComponent.Param(
      label = "Status",
      name = "status",
      type = ReportParameterType.ENUM,
            enumClass = RideStatus.class,
      required = true,
      list = true,
      defaultValue = "[\"COMPLETED\"]",
      order = 1
    ),
    @ReportComponent.Param(
      label = "Rider email",
      name = "riderEmail",
      type = ReportParameterType.STRING,
      order = 2
    ),
    @ReportComponent.Param(
      label = "Driver email",
      name = "driverEmail",
      type = ReportParameterType.STRING,
      order = 3
    ),
    @ReportComponent.Param(
      label = "Rider ID",
      name = "riderId",
      type = ReportParameterType.INTEGER,
      order = 4
    ),
    @ReportComponent.Param(
      label = "Driver ID",
      name = "driverId",
      type = ReportParameterType.INTEGER,
      order = 5
    ),
    @ReportComponent.Param(
      label = "Charged?",
      name = "charged",
      type = ReportParameterType.BOOLEAN,
      order = 6
    ),
    @ReportComponent.Param(
      label = "Created after",
      name = "createdOnAfter",
      type = ReportParameterType.DATETIME,
      order = 7
    ),
    @ReportComponent.Param(
      label = "Created before",
      name = "createdOnBefore",
      type = ReportParameterType.DATETIME,
      order = 8
    ),
    @ReportComponent.Param(
      label = "Completed after",
      name = "completedOnAfter",
      type = ReportParameterType.DATETIME,
      order = 9
    ),
    @ReportComponent.Param(
      label = "Completed before",
      name = "completedOnBefore",
      type = ReportParameterType.DATETIME,
      order = 10
    ),
    @ReportComponent.Param(
      label = "Cancelled after",
      name = "cancelledOnAfter",
      type = ReportParameterType.DATETIME,
      order = 11
    ),
    @ReportComponent.Param(
      label = "Cancelled before",
      name = "cancelledOnBefore",
      type = ReportParameterType.DATETIME,
      order = 12
    ),
    @ReportComponent.Param(
      label = "Phone number",
      name = "phoneNumber",
      type = ReportParameterType.STRING,
      order = 13
    ),
    @ReportComponent.Param(
      label = "City ID",
      name = "cityId",
      type = ReportParameterType.INTEGER,
      order = 14
    )
  }
)
public class RidesExportReport extends BaseReport<RidesExportReportEntry, RidesExportReportParams> {

  private RideReportDslRepository rideReportDslRepository;

  @Inject
  public RidesExportReport(RideReportDslRepository rideReportDslRepository) {
    this.rideReportDslRepository = rideReportDslRepository;
  }

  @Override
  protected ReportAdapter<RidesExportReportEntry> createAdapter() {
    List<Pair<Instant, Instant>> instantPairs = ImmutableList.of(
      ImmutablePair.of(parameters.getCancelledOnAfter(), parameters.getCancelledOnBefore()),
      ImmutablePair.of(parameters.getCompletedOnAfter(), parameters.getCompletedOnBefore()),
      ImmutablePair.of(parameters.getCreatedOnAfter(), parameters.getCreatedOnBefore())
    );
    String period = null;
    for (Pair<Instant, Instant> instants : instantPairs) {
      period = tryGetPeriod(instants.getLeft(), instants.getRight());
      if (period != null) {
        break;
      }
    }
    return new DefaultReportAdapter<>(RidesExportReportEntry.class, ImmutableMap.of("period", period));
  }

  @Override
  protected void doExecute() {
    this.resultsStream = rideReportDslRepository.exportRides(parameters.asListRidesParams()).stream();
  }

  private String tryGetPeriod(Instant start, Instant end) {
    String period = null;
    if (start != null && end != null) {
      LocalDate startDate = LocalDateTime.ofInstant(start, Constants.CST_ZONE).toLocalDate();
      LocalDate endDate = LocalDateTime.ofInstant(end, Constants.CST_ZONE).toLocalDate();
      if (startDate.equals(endDate)) {
        period = startDate.format(Constants.DATE_FORMATTER);
      } else {
        period = String.format("%s - %s", startDate.format(Constants.DATE_FORMATTER), endDate.format(Constants.DATE_FORMATTER));
      }
    }
    return period;
  }

}
