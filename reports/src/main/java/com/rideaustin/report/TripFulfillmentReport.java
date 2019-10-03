package com.rideaustin.report;

import static com.rideaustin.model.enums.RideStatus.ADMIN_CANCELLED;
import static com.rideaustin.model.enums.RideStatus.COMPLETED;
import static com.rideaustin.model.enums.RideStatus.DRIVER_CANCELLED;
import static com.rideaustin.model.enums.RideStatus.NO_AVAILABLE_DRIVER;
import static com.rideaustin.model.enums.RideStatus.RIDER_CANCELLED;

import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.joda.time.DateTimeZone;
import org.joda.time.Interval;

import com.google.common.collect.ImmutableMap;
import com.rideaustin.model.enums.RideStatus;
import com.rideaustin.model.reports.TripFulfillmentQueryResultEntry;
import com.rideaustin.repo.dsl.RideReportDslRepository;
import com.rideaustin.report.adapter.DefaultReportAdapter;
import com.rideaustin.report.adapter.ReportAdapter;
import com.rideaustin.report.entry.TripFulfillmentReportEntry;
import com.rideaustin.report.model.ReportComponent;
import com.rideaustin.report.model.ReportParameterType;
import com.rideaustin.report.params.TripFulfillmentReportParams;

@ReportComponent(
  id = 2,
  name = "Trip fulfillment report",
  description = "Trip fulfillment report",
  header = "Trip fulfillment report for {startDateTime} - {endDateTime}",
  parameters = {
    @ReportComponent.Param(
      label = "Start date",
      name = "startDateTime",
      type = ReportParameterType.DATETIME,
      required = true,
      order = 1
    ),
    @ReportComponent.Param(
      label = "End date",
      name = "endDateTime",
      type = ReportParameterType.DATETIME,
      required = true,
      order = 2
    ),
    @ReportComponent.Param(
      label = "Interval",
      name = "interval",
      description = "Interval in minutes",
      type = ReportParameterType.INTEGER,
      order = 3
    )
  }
)
public class TripFulfillmentReport extends BaseReport<TripFulfillmentReportEntry, TripFulfillmentReportParams>
  implements Report<TripFulfillmentReportEntry, TripFulfillmentReportParams> {

  private static final int MILLISECONDS_IN_MINUTE = 60000;
  private RideReportDslRepository rideReportDslRepository;
  private static final BiFunction<Map<RideStatus, Long>, Map<RideStatus, Long>, Map<RideStatus, Long>> SUM_BY_RIDE_STATUS_REMAPPER = (v1, v2) -> {
    for (Map.Entry<RideStatus, Long> e : v2.entrySet()) {
      v1.computeIfPresent(e.getKey(), (status, count) -> count = count + e.getValue());
      v1.putIfAbsent(e.getKey(), e.getValue());
    }
    return v1;
  };

  @Inject
  public TripFulfillmentReport(RideReportDslRepository rideReportDslRepository) {
    this.rideReportDslRepository = rideReportDslRepository;
  }

  @Override
  protected ReportAdapter<TripFulfillmentReportEntry> createAdapter() {
    return new DefaultReportAdapter<>(TripFulfillmentReportEntry.class, ImmutableMap.of(
      "startDateTime", parameters.getStartDateTime(),
      "endDateTime", parameters.getEndDateTime()
    ));
  }

  @Override
  protected void doExecute() {
    List<TripFulfillmentQueryResultEntry> tripFulfillmentReport = rideReportDslRepository.getTripFulfillmentReport(parameters.getStartDateTime(), parameters.getEndDateTime());
    Map<Instant, Map<RideStatus, Long>> statusMap = mapStatusesByDate(tripFulfillmentReport);

    long startDateTimeMillis = getUTCDateTimeMillis(parameters.getStartDateTime());

    Map<Interval, Map<RideStatus, Long>> reportEntriesMapping = groupStatusesByInterval(statusMap, startDateTimeMillis,
      getGroupingIntervalDuration(startDateTimeMillis));

    this.resultsStream = reportEntriesMapping.entrySet().stream()
      .map(e -> {
        Map<RideStatus, Long> value = e.getValue();
        Interval interval = e.getKey();
        return new TripFulfillmentReportEntry(interval, Optional.ofNullable(value.get(RideStatus.COMPLETED)).orElse(0L),
          Optional.ofNullable(value.get(RideStatus.RIDER_CANCELLED)).orElse(0L),
          Optional.ofNullable(value.get(RideStatus.DRIVER_CANCELLED)).orElse(0L),
          Optional.ofNullable(value.get(RideStatus.ADMIN_CANCELLED)).orElse(0L),
          Optional.ofNullable(value.get(RideStatus.NO_AVAILABLE_DRIVER)).orElse(0L)
        );
      })
      .sorted(Comparator.comparing(e -> e.getInterval().getStartMillis()));
  }

  private Map<Instant, Map<RideStatus, Long>> mapStatusesByDate(List<TripFulfillmentQueryResultEntry> tripFulfillmentReport) {
    return tripFulfillmentReport.stream().collect(
      Collectors.toMap(e -> {
          Instant dateTime;
          switch (e.getStatus()) {
            case COMPLETED:
              dateTime = e.getCompletedOn();
              break;
            case ADMIN_CANCELLED:
            case DRIVER_CANCELLED:
            case RIDER_CANCELLED:
              dateTime = e.getCancelledOn();
              break;
            case NO_AVAILABLE_DRIVER:
              dateTime = e.getCreatedOn();
              break;
            default:
              throw new IllegalArgumentException(String.format("Unknown ride terminal status %s", e));
          }
          return dateTime;
        },
        e -> {
          Map<RideStatus, Long> value = new EnumMap<>(RideStatus.class);
          value.put(e.getStatus(), 1L);
          return value;
        },
        SUM_BY_RIDE_STATUS_REMAPPER::apply)
    );
  }

  private Map<Interval, Map<RideStatus, Long>> groupStatusesByInterval(Map<Instant, Map<RideStatus, Long>> statusMap, long startDateTimeMillis, Long groupingIntervalDuration) {
    Map<Interval, Map<RideStatus, Long>> reportEntriesMapping = new HashMap<>();
    for (Map.Entry<Instant, Map<RideStatus, Long>> entry : statusMap.entrySet()) {
      Interval interval = new Interval(startDateTimeMillis, startDateTimeMillis + groupingIntervalDuration, DateTimeZone.UTC);
      while (!interval.contains(entry.getKey().toEpochMilli())) {
        interval = interval.withStart(interval.getEnd()).withEndMillis(interval.getEndMillis() + groupingIntervalDuration);
      }
      reportEntriesMapping.merge(interval, entry.getValue(), SUM_BY_RIDE_STATUS_REMAPPER);
    }
    return reportEntriesMapping;
  }

  private Long getGroupingIntervalDuration(long startDateTimeMillis) {
    Optional<Long> groupingIntervalDuration = Optional.ofNullable(parameters.getInterval());
    return groupingIntervalDuration
      .map(e -> e * MILLISECONDS_IN_MINUTE)
      .orElse(getUTCDateTimeMillis(parameters.getEndDateTime()) - startDateTimeMillis);
  }

  private long getUTCDateTimeMillis(Instant instant) {
    return instant.atOffset(ZoneOffset.UTC).toInstant().toEpochMilli();
  }

}
