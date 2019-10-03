package com.rideaustin.report;

import static com.rideaustin.utils.SafeZeroUtils.safeZero;
import static java.util.stream.Collectors.groupingBy;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

import javax.inject.Inject;

import com.google.common.collect.ImmutableMap;
import com.rideaustin.Constants;
import com.rideaustin.model.reports.WeeklyStatisticsReportResultEntry;
import com.rideaustin.repo.dsl.RideReportDslRepository;
import com.rideaustin.report.adapter.DefaultReportAdapter;
import com.rideaustin.report.adapter.ReportAdapter;
import com.rideaustin.report.entry.WeeklyStatisticsReportEntry;
import com.rideaustin.report.model.ReportComponent;
import com.rideaustin.report.model.ReportParameterType;
import com.rideaustin.report.params.WeeklyStatisticsReportParams;
import com.rideaustin.utils.DateUtils;

import lombok.RequiredArgsConstructor;

@ReportComponent(
  id = 11,
  name = "Trip counts by car category report",
  header = "Trip counts by car category report - All trips through {endDate}",
  parameters = {
    @ReportComponent.Param(
      label = "Completed on before",
      name = "completedOnBefore",
      type = ReportParameterType.DATETIME,
      order = 1
    )
  }
)
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class WeeklyStatisticsReport extends BaseReport<WeeklyStatisticsReportEntry, WeeklyStatisticsReportParams> {

  private final RideReportDslRepository rideReportDslRepository;

  @Override
  protected ReportAdapter<WeeklyStatisticsReportEntry> createAdapter() {
    return new DefaultReportAdapter<>(WeeklyStatisticsReportEntry.class, ImmutableMap.of("endDate", parameters.getCompletedOnBefore()));
  }

  @Override
  protected void doExecute() {
    List<WeeklyStatisticsReportResultEntry> rawData = rideReportDslRepository.getWeeklyStatisticsRaw(parameters.getCompletedOnBefore());
    Map<LocalDate, Map<String, List<WeeklyStatisticsReportResultEntry>>> groupedData = rawData.stream()
      .collect(
        groupingBy(
          e -> DateUtils.getEndOfWeek(e.getCompletedOn()),
          groupingBy(WeeklyStatisticsReportResultEntry::getCarCategory)
        )
      );
    List<WeeklyStatisticsReportEntry> results = new ArrayList<>();
    for (Map.Entry<LocalDate, Map<String, List<WeeklyStatisticsReportResultEntry>>> weekEntry : groupedData.entrySet()) {
      String week = weekEntry.getKey().format(Constants.DATE_FORMATTER);
      for (Map.Entry<String, List<WeeklyStatisticsReportResultEntry>> categoryEntry : weekEntry.getValue().entrySet()) {
        String carCategory = categoryEntry.getKey();
        List<WeeklyStatisticsReportResultEntry> data = categoryEntry.getValue();
        Long priorityTripsCount = data.stream().filter(e -> e.getSurgeFactor().compareTo(BigDecimal.ONE) > 0).count();
        Long regularTripsCount = data.size() - priorityTripsCount;
        BigDecimal grossFare = sum(data.stream(), grossFare());
        BigDecimal surgeFare = sum(data.stream(), WeeklyStatisticsReportResultEntry::getSurgeFare);
        BigDecimal charityRoundUp = sum(data.stream(), WeeklyStatisticsReportResultEntry::getRoundUp);
        BigDecimal driverPayment = sum(data.stream(), driverPayment());
        BigDecimal cancellationFees = sum(data.stream(), WeeklyStatisticsReportResultEntry::getCancellationFee);
        BigDecimal tips = sum(data.stream(), WeeklyStatisticsReportResultEntry::getTip);
        BigDecimal priorityFare = sum(data.stream(), fallbackFare());
        BigDecimal milesDriven = sum(data.stream(), WeeklyStatisticsReportResultEntry::getDistanceTravelled);
        results.add(new WeeklyStatisticsReportEntry(week, carCategory, regularTripsCount, priorityTripsCount, grossFare,
          surgeFare, charityRoundUp, driverPayment, cancellationFees, tips, priorityFare, milesDriven));
      }
    }
    this.resultsStream = results.stream().sorted(Comparator.comparing(WeeklyStatisticsReportEntry::getWeek).thenComparing(WeeklyStatisticsReportEntry::getCarCategory));
  }

  private BigDecimal sum(Stream<WeeklyStatisticsReportResultEntry> dataStream, Function<WeeklyStatisticsReportResultEntry, BigDecimal> mapper) {
    return dataStream.map(e -> safeZero(mapper.apply(e))).reduce(BigDecimal.ZERO, BigDecimal::add);
  }

  private Function<WeeklyStatisticsReportResultEntry, BigDecimal> grossFare() {
    return e -> safeZero(e.getSubTotal()).add(safeZero(e.getBookingFee())).add(safeZero(e.getCityFee())).add(safeZero(e.getTip()));
  }

  private Function<WeeklyStatisticsReportResultEntry, BigDecimal> driverPayment() {
    return e -> {
      if (e.getNormalFare() == null) {
        return safeZero(e.getDriverPayment()).subtract(safeZero(e.getTip()));
      }
      return fallbackFare().apply(e);
    };
  }

  private Function<WeeklyStatisticsReportResultEntry, BigDecimal> fallbackFare() {
    return e -> safeZero(e.getNormalFare()).subtract(e.getRaFixedFee());
  }
}
