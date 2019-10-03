package com.rideaustin.report;

import static com.rideaustin.report.TNCCompositeReport.extractTimeBlock;
import static com.rideaustin.report.TNCCompositeReport.getTimeBlockStart;
import static java.util.stream.Collectors.toMap;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import javax.inject.Inject;

import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;

import com.google.common.collect.ImmutableMap;
import com.rideaustin.Constants;
import com.rideaustin.repo.dsl.DriverReportDslRepository;
import com.rideaustin.repo.dsl.RideReportDslRepository;
import com.rideaustin.report.adapter.DefaultReportAdapter;
import com.rideaustin.report.adapter.ReportAdapter;
import com.rideaustin.report.entry.SurgeHistoryEntry;
import com.rideaustin.report.entry.TNCSummaryReportEntry;
import com.rideaustin.report.model.NestedReport;
import com.rideaustin.report.params.TNCCompositeReportParams;

@NestedReport
public class TNCSummaryReport extends BaseReport<TNCSummaryReportEntry, TNCCompositeReportParams> {

  private static final int TIME_BLOCK_DURATION = 14400;
  private RideReportDslRepository rideReportDslRepository;
  private DriverReportDslRepository driverReportDslRepository;

  @Inject
  public TNCSummaryReport(RideReportDslRepository rideReportDslRepository, DriverReportDslRepository driverReportDslRepository) {
    this.rideReportDslRepository = rideReportDslRepository;
    this.driverReportDslRepository = driverReportDslRepository;
  }

  @Override
  protected ReportAdapter<TNCSummaryReportEntry> createAdapter() {
    return new DefaultReportAdapter<>(TNCSummaryReportEntry.class, ImmutableMap.of(
      "startDate", parameters.getStartDate(),
      "endDate", parameters.getEndDate()
    ));
  }

  @Override
  protected void doExecute() {
    Instant startDate = parameters.getStartDate();
    Instant endDate = parameters.getEndDate();
    TNCSummaryReportEntry tncSummary = rideReportDslRepository.getTNCSummary(startDate, endDate);
    Long driversLoggedOnCount = driverReportDslRepository.getDriversLoggedOnCount(startDate, endDate);
    tncSummary.setVehiclesLoggedIntoPlatform(driversLoggedOnCount);
    List<SurgeHistoryEntry> surgeRidesTimeSpans = rideReportDslRepository.getSurgeAreaHistory(startDate, endDate);
    tncSummary.setHoursSurgePricingInEffect(calculateSurgeTime(surgeRidesTimeSpans));

    this.resultsStream = Stream.of(tncSummary);
  }

  private Map<String, BigDecimal> calculateSurgeTime(List<SurgeHistoryEntry> surgeHistoryEntries) {
    Instant start = null;
    List<Duration> durations = new ArrayList<>();
    Map<String, Long> result = new HashMap<>();
    for (int i = 0; i < surgeHistoryEntries.size(); i++) {
      SurgeHistoryEntry entry = surgeHistoryEntries.get(i);
      boolean isNeutral = entry.getSurgeFactor().compareTo(BigDecimal.ONE) == 0;
      if (isNeutral && start == null) {
        continue;
      } else if (!isNeutral && start != null) {
        addDuration(durations, entry, surgeHistoryEntries.get(i - 1));
      } else if (!isNeutral && start == null) {
        start = entry.getCreatedDate();
      } else {
        start = null;
        addDuration(durations, entry, surgeHistoryEntries.get(i - 1));
      }
    }
    for (int i = 0; i < durations.size(); i++) {
      Duration duration = durations.get(i);
      String startBlock = extractTimeBlock(duration.getStart());
      String endBlock = extractTimeBlock(duration.getEnd());
      if (!startBlock.equals(endBlock)) {
        Instant threshold = getTimeBlockStart(duration.getStart()).plusSeconds(TIME_BLOCK_DURATION);
        durations.remove(i);
        durations.add(i, new Duration(duration.getStart(), threshold.minusSeconds(1)));
        durations.add(i+1, new Duration(threshold, duration.getEnd()));
        i--;
      }
    }
    for (Duration duration : durations) {
      updateResult(result, extractTimeBlock(duration.getStart()), duration.toSeconds());
    }
    return result.entrySet().stream().collect(toMap(
      Map.Entry::getKey,
      e -> BigDecimal.valueOf(e.getValue()).divide(Constants.SECONDS_PER_HOUR, 2, Constants.ROUNDING_MODE)
    ));
  }

  private void addDuration(List<Duration> durations, SurgeHistoryEntry entry, SurgeHistoryEntry previous) {
    Optional<Duration> existing = durations.stream().filter(d -> within(entry, d)).findFirst();
    if (!existing.isPresent()) {
      Optional<Duration> last = durations.stream().filter(d -> d.getEnd().equals(previous.getCreatedDate())).findFirst();
      if (last.isPresent()) {
        last.get().setEnd(entry.getCreatedDate());
      } else {
        durations.add(new Duration(previous.getCreatedDate(), entry.getCreatedDate()));
      }
    }
  }

  private void updateResult(Map<String, Long> result, String timeBlock, long seconds) {
    long base = 0;
    if (result.containsKey(timeBlock)) {
      base = result.get(timeBlock);
    }
    result.put(timeBlock, base + seconds);
  }

  private boolean within(SurgeHistoryEntry entry, Duration d) {
    return entry.getCreatedDate().isAfter(d.getStart()) && entry.getCreatedDate().isBefore(d.getEnd());
  }

  private static class Duration {

    private final Pair<Instant, Instant> pair;

    public Duration(Instant start, Instant end) {
      pair = MutablePair.of(start, end);
    }

    public Instant getStart() {
      return pair.getLeft();
    }

    public Instant getEnd() {
      return pair.getRight();
    }

    public void setEnd(Instant instant) {
      pair.setValue(instant);
    }

    public long toSeconds() {
      return getEnd().getEpochSecond() - getStart().getEpochSecond();
    }

  }

}
