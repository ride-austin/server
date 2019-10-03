package com.rideaustin.report;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.rideaustin.Constants;
import com.rideaustin.report.adapter.DefaultReportAdapter;
import com.rideaustin.report.adapter.ReportAdapter;
import com.rideaustin.report.entry.CompositeReportEntry;
import com.rideaustin.report.model.ReportComponent;
import com.rideaustin.report.model.ReportParameterType;
import com.rideaustin.report.params.TNCCompositeReportParams;

@ReportComponent(
  id = 5,
  name = "TNC City report",
  description = "TNC City report",
  header ="TNC City report for {startDate} - {endDate}",
  parameters = {
    @ReportComponent.Param(
      label ="Start date",
      name = "startDate",
      type = ReportParameterType.DATE,
      required = true,
      order = 1
    ),
    @ReportComponent.Param(
      label ="End date",
      name = "endDate",
      type = ReportParameterType.DATE,
      required = true,
      order = 2
    ),
  }
)
public class TNCCompositeReport extends BaseCompositeReport<TNCCompositeReportParams> {

  public static final String TWO_AM_SIX_AM = "2:00AM - 6:00AM";
  public static final String SIX_AM_TEN_AM = "6:00AM - 10:00AM";
  public static final String TEN_AM_TWO_PM = "10:00AM - 2:00PM";
  public static final String TWO_PM_SIX_PM = "2:00PM - 6:00PM";
  public static final String SIX_PM_TEN_PM = "6:00PM - 10:00PM";
  public static final String TEN_PM_TWO_AM = "10:00PM - 2:00AM";

  static final Map<List<Integer>, String> HOUR_BLOCK_MAPPING = ImmutableMap.<List<Integer>, String>builder()
    .put(ImmutableList.of(2, 3, 4, 5), TWO_AM_SIX_AM)
    .put(ImmutableList.of(6, 7, 8, 9), SIX_AM_TEN_AM)
    .put(ImmutableList.of(10, 11, 12, 13), TEN_AM_TWO_PM)
    .put(ImmutableList.of(14, 15, 16, 17), TWO_PM_SIX_PM)
    .put(ImmutableList.of(18, 19, 20, 21), SIX_PM_TEN_PM)
    .put(ImmutableList.of(22, 23, 0, 1), TEN_PM_TWO_AM)
    .build();

  @Inject
  public TNCCompositeReport(TNCTripReport tripReport, TNCDriversHoursLoggedOnReport driversHoursLoggedOnReport,
    TNCSummaryReport summaryReport, TNCDriverStatsReport driverStatsReport) {
    super(tripReport, driversHoursLoggedOnReport, summaryReport, driverStatsReport);
  }

  @Override
  protected ReportAdapter<CompositeReportEntry> createAdapter() {
    return new DefaultReportAdapter<>(CompositeReportEntry.class,
      ImmutableMap.of(
        "startDate", parameters.getStartDate(),
        "endDate", parameters.getEndDate()
      )
    );
  }

  static String extractTimeBlock(Instant date) {
    int hour = date.atZone(Constants.CST_ZONE).get(ChronoField.HOUR_OF_DAY);
    return HOUR_BLOCK_MAPPING.entrySet().stream().filter(e -> e.getKey().contains(hour)).findFirst().map(Map.Entry::getValue).orElse("");
  }

  static Instant getTimeBlockStart(Instant date) {
    int hour = date.atZone(Constants.CST_ZONE).get(ChronoField.HOUR_OF_DAY);
    int startHour = HOUR_BLOCK_MAPPING.entrySet().stream().filter(e -> e.getKey().contains(hour)).findFirst().map(Map.Entry::getKey).get().get(0);
    ZonedDateTime zonedDateTime = date.atZone(Constants.CST_ZONE);
    //if block starts the day before subtract 1 day
    if (hour < startHour && startHour == 22) {
      zonedDateTime = zonedDateTime.minusDays(1);
    }
    return zonedDateTime.withHour(startHour).withMinute(0).withSecond(0).toInstant().atZone(ZoneId.of("UTC")).toInstant();
  }
}
