package com.rideaustin.report;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.inject.Inject;

import org.apache.commons.collections.CollectionUtils;

import com.rideaustin.repo.dsl.UniqueRidersDriversReportRepository;
import com.rideaustin.report.adapter.DefaultReportAdapter;
import com.rideaustin.report.adapter.ReportAdapter;
import com.rideaustin.report.entry.UniqueRidersDriversReportEntry;
import com.rideaustin.report.model.ReportComponent;
import com.rideaustin.report.params.NullReportParams;

@ReportComponent(
  id = 12,
  name = "Unique Riders & Drivers by Week Report",
  header = "Unique Riders & Drivers by Week"
)
public class UniqueRidersDriversReport extends BaseReport<UniqueRidersDriversReportEntry, NullReportParams> {

  private UniqueRidersDriversReportRepository reportRepository;

  @Inject
  public UniqueRidersDriversReport(UniqueRidersDriversReportRepository reportRepository) {
    this.reportRepository = reportRepository;
  }

  @Override
  protected ReportAdapter<UniqueRidersDriversReportEntry> createAdapter() {
    return new DefaultReportAdapter<>(UniqueRidersDriversReportEntry.class, Collections.emptyMap());
  }

  @Override
  protected void doExecute() {
    Map<LocalDate, Long> driversSignedUpByWeek = reportRepository.findDriversSignedUpByWeek();
    Map<LocalDate, Long> firstTimeDrivers = reportRepository.findFirstTimeDrivers();
    Map<LocalDate, Long> uniqueDrivers = reportRepository.findUniqueDrivers();
    Map<LocalDate, Long> ridersSignedUpByWeek = reportRepository.findRidersSignedUpByWeek();
    Map<LocalDate, Long> firstTimeRiders = reportRepository.findFirstTimeRiders();
    Map<LocalDate, Long> uniqueRiders = reportRepository.findUniqueRiders();
    Map<LocalDate, BigDecimal> onlineHours = reportRepository.sumOnlineHours();
    Map<LocalDate, BigDecimal> drivenHours = reportRepository.sumDrivenHours();

    Set<LocalDate> dates = new TreeSet<>(CollectionUtils.intersection(driversSignedUpByWeek.keySet(),
      CollectionUtils.intersection(ridersSignedUpByWeek.keySet(), CollectionUtils.intersection(onlineHours.keySet(), drivenHours.keySet()))));

    List<UniqueRidersDriversReportEntry> result = new ArrayList<>();
    long totalDriversSignedUp = 0L;
    long totalRidersSignedUp = 0L;
    for (LocalDate date : dates) {
      Long driversSignedUpThisWeek = driversSignedUpByWeek.getOrDefault(date, 0L);
      Long ridersSignedUpThisWeek = ridersSignedUpByWeek.getOrDefault(date, 0L);
      totalDriversSignedUp += driversSignedUpThisWeek;
      totalRidersSignedUp += ridersSignedUpThisWeek;
      result.add(new UniqueRidersDriversReportEntry(date, totalDriversSignedUp, driversSignedUpThisWeek,
        uniqueDrivers.getOrDefault(date, 0L), totalRidersSignedUp, ridersSignedUpThisWeek,
        uniqueRiders.getOrDefault(date, 0L), firstTimeDrivers.getOrDefault(date, 0L), firstTimeRiders.getOrDefault(date, 0L),
        drivenHours.getOrDefault(date, BigDecimal.ZERO), onlineHours.getOrDefault(date, BigDecimal.ZERO)));
    }

    this.resultsStream = result.stream().sorted(Comparator.comparing(UniqueRidersDriversReportEntry::getEndOfWeek));
  }
}
