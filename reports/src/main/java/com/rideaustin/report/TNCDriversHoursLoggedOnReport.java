package com.rideaustin.report;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;

import com.google.common.collect.ImmutableMap;
import com.rideaustin.Constants;
import com.rideaustin.model.reports.TNCDriversHoursLoggedOnReportResult;
import com.rideaustin.repo.dsl.DriverReportDslRepository;
import com.rideaustin.report.adapter.DefaultReportAdapter;
import com.rideaustin.report.adapter.ReportAdapter;
import com.rideaustin.report.entry.TNCDriversHoursLoggedOnReportEntry;
import com.rideaustin.report.model.NestedReport;
import com.rideaustin.report.params.TNCCompositeReportParams;

@NestedReport
public class TNCDriversHoursLoggedOnReport extends BaseReport<TNCDriversHoursLoggedOnReportEntry, TNCCompositeReportParams> {

  private DriverReportDslRepository driverReportDslRepository;

  @Inject
  public TNCDriversHoursLoggedOnReport(DriverReportDslRepository driverReportDslRepository) {
    this.driverReportDslRepository = driverReportDslRepository;
  }

  @Override
  protected ReportAdapter<TNCDriversHoursLoggedOnReportEntry> createAdapter() {
    return new DefaultReportAdapter<>(TNCDriversHoursLoggedOnReportEntry.class, ImmutableMap.of(
      "startDate", parameters.getStartDate(),
      "endDate", parameters.getEndDate()
    ));
  }

  @Override
  protected void doExecute() {
    Set<TNCDriversHoursLoggedOnReportResult> driversLoggedOn =
      driverReportDslRepository.getDriversLoggedOn(parameters.getStartDate(), parameters.getEndDate());
    Map<LocalDate, Integer> driversCounts = driversLoggedOn.stream()
      .collect(groupingBy(
        e -> extractLocalDate(e.getCreatedDate()),
        collectingAndThen(mapping(TNCDriversHoursLoggedOnReportResult::getId, Collectors.toSet()), Set::size)
      ));
    Map<LocalDate, TNCDriversHoursLoggedOnReportEntry> map = new HashMap<>();
    for (TNCDriversHoursLoggedOnReportResult result : driversLoggedOn) {
      LocalDate key = extractLocalDate(result.getCreatedDate());
      BigDecimal duration = BigDecimal.valueOf(result.getLocationUpdatedDate().toEpochMilli() - result.getCreatedDate().toEpochMilli());
      double durationHours = Constants.HOURS_PER_MILLISECOND.multiply(duration).doubleValue();
      map.computeIfPresent(key, (k,v) -> {
        fillReportEntry(v, v.getDriverHoursLoggedOn()+durationHours, driversCounts.get(k));
        return v;
      });
      map.computeIfAbsent(key, k -> {
        TNCDriversHoursLoggedOnReportEntry v = new TNCDriversHoursLoggedOnReportEntry();
        v.setDate(k);
        fillReportEntry(v, durationHours, driversCounts.get(k));
        return v;
      });
    }
    this.resultsStream = map.values().stream().sorted(Comparator.comparing(TNCDriversHoursLoggedOnReportEntry::getDate));
  }

  private void fillReportEntry(TNCDriversHoursLoggedOnReportEntry entry, double duration, Integer numberOfDrivers) {
    entry.setDriverHoursLoggedOn(duration);
    entry.setNumberOfDrivers(numberOfDrivers);
  }

  private LocalDate extractLocalDate(Instant instant) {
    return LocalDate.from(instant.atZone(Constants.CST_ZONE));
  }
}
