package com.rideaustin.report;

import static com.rideaustin.Constants.CST_ZONE;
import static com.rideaustin.report.TNCCompositeReport.HOUR_BLOCK_MAPPING;
import static com.rideaustin.report.TNCCompositeReport.extractTimeBlock;
import static java.util.Comparator.comparing;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;

import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collector;

import javax.inject.Inject;

import org.apache.commons.lang3.tuple.ImmutablePair;

import com.google.common.collect.ImmutableMap;
import com.rideaustin.model.enums.RideStatus;
import com.rideaustin.model.reports.TNCTripReportResult;
import com.rideaustin.repo.dsl.RideReportDslRepository;
import com.rideaustin.report.adapter.DefaultReportAdapter;
import com.rideaustin.report.adapter.ReportAdapter;
import com.rideaustin.report.entry.TNCTripReportEntry;
import com.rideaustin.report.model.NestedReport;
import com.rideaustin.report.params.TNCCompositeReportParams;

import lombok.RequiredArgsConstructor;

@NestedReport
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class TNCTripReport extends BaseReport<TNCTripReportEntry, TNCCompositeReportParams> {

  private final RideReportDslRepository rideReportDslRepository;

  @Override
  protected ReportAdapter<TNCTripReportEntry> createAdapter() {
    return new DefaultReportAdapter<>(TNCTripReportEntry.class,
      ImmutableMap.of(
        "startDate", parameters.getStartDate(),
        "endDate", parameters.getEndDate()
      )
    );
  }

  @Override
  protected void doExecute() {
    EnumSet<RideStatus> notServicedStatusSet = EnumSet.of(RideStatus.RIDER_CANCELLED, RideStatus.DRIVER_CANCELLED,
      RideStatus.ADMIN_CANCELLED, RideStatus.NO_AVAILABLE_DRIVER);
    Set<TNCTripReportResult> resultSet = rideReportDslRepository.getTNCTripReportResultSet(parameters.getStartDate(),
      parameters.getEndDate());

    Map<String, Map<String, Long>> requestsMap = resultSet.stream()
      .collect(groupingBy(
        result -> extractTimeBlock(result.getCreatedDate()),
        aggregateCountByZipCode(TNCTripReportResult::getStartZipCode)
      ));

    Map<String, Map<String, Long>> notServicedRequestsMap = resultSet.stream()
      .filter(r -> notServicedStatusSet.contains(r.getRideStatus()))
      .collect(groupingBy(
        result -> extractTimeBlock(result.getCreatedDate()),
        aggregateCountByZipCode(TNCTripReportResult::getStartZipCode)
      ));

    Map<String, Map<String, Long>> pickupsMap = resultSet.stream()
      .filter(r -> RideStatus.COMPLETED.equals(r.getRideStatus()))
      .collect(groupingBy(
        result -> extractTimeBlock(result.getCreatedDate()),
        aggregateCountByZipCode(TNCTripReportResult::getStartZipCode)
      ));

    Map<String, Map<String, Long>> dropoffsMap = resultSet.stream()
      .filter(r -> RideStatus.COMPLETED.equals(r.getRideStatus()))
      .collect(groupingBy(
        result -> extractTimeBlock(result.getCompletedOn()),
        aggregateCountByZipCode(TNCTripReportResult::getEndZipCode)
      ));

    Map<ImmutablePair<String, String>, TNCTripReportEntry> flattened = flattenRequestsMap(requestsMap);
    fillFlattenedMap(flattened, notServicedRequestsMap, TNCTripReportEntry::setRequestsNotServiced);
    fillFlattenedMap(flattened, pickupsMap, TNCTripReportEntry::setPickups);
    fillFlattenedMap(flattened, dropoffsMap, TNCTripReportEntry::setDropoffs);

    this.resultsStream = flattened.entrySet()
      .stream()
      .map(e -> {
        e.getValue().setZipCode(e.getKey().getRight());
        e.getValue().setTimeBlock(e.getKey().getLeft());
        String reportingMonth = parameters.getStartDate().atZone(CST_ZONE).getMonth()
          .getDisplayName(TextStyle.FULL, Locale.ENGLISH);
        e.getValue().setReportingMonth(reportingMonth);
        return e.getValue();
      })
      .sorted(comparing(TNCTripReportEntry::getTimeBlock, (o1, o2) -> {
        ArrayList<String> ordered = new ArrayList<>(HOUR_BLOCK_MAPPING.values());
        Integer i1 = ordered.indexOf(o1);
        Integer i2 = ordered.indexOf(o2);
        return i1.compareTo(i2);
      }).thenComparing(TNCTripReportEntry::getZipCode));
  }

  private Map<ImmutablePair<String, String>, TNCTripReportEntry> flattenRequestsMap(Map<String, Map<String, Long>> requestsMap) {
    Map<ImmutablePair<String, String>, TNCTripReportEntry> flattened = new HashMap<>();
    for (Entry<String, Map<String, Long>> timeBlockEntry : requestsMap.entrySet()) {
      for (Entry<String, Long> zipCodeEntry : timeBlockEntry.getValue().entrySet()) {
        TNCTripReportEntry reportEntry = new TNCTripReportEntry();
        reportEntry.setRequests(zipCodeEntry.getValue());
        ImmutablePair<String, String> key = ImmutablePair.of(timeBlockEntry.getKey(), zipCodeEntry.getKey());
        flattened.put(key, reportEntry);
      }
    }
    return flattened;
  }

  private void fillFlattenedMap(Map<ImmutablePair<String, String>, TNCTripReportEntry> flattened,
    Map<String, Map<String, Long>> sourceMap,
    BiConsumer<TNCTripReportEntry, Long> setter) {
    for (Entry<String, Map<String, Long>> timeBlockEntry : sourceMap.entrySet()) {
      for (Entry<String, Long> zipCodeEntry : timeBlockEntry.getValue().entrySet()) {
        ImmutablePair<String, String> key = ImmutablePair.of(timeBlockEntry.getKey(), zipCodeEntry.getKey());
        Long value = zipCodeEntry.getValue();
        flattened.computeIfPresent(key, (k, v) -> {
          setter.accept(v, value);
          return v;
        });
        flattened.computeIfAbsent(key, k -> {
          TNCTripReportEntry reportEntry = new TNCTripReportEntry();
          setter.accept(reportEntry, value);
          return reportEntry;
        });
      }
    }
  }

  private Collector<TNCTripReportResult, ?, Map<String, Long>> aggregateCountByZipCode(Function<TNCTripReportResult, String> zipCodeExtractor) {
    Function<TNCTripReportResult, String> safeZipCodeExtractor = e -> Optional.ofNullable(zipCodeExtractor.apply(e))
      .map(z -> z.replaceAll("[^0-9]", "")).orElse("");
    return mapping(identity(), groupingBy(safeZipCodeExtractor, mapping(identity(), counting())));
  }
}
