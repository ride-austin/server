package com.rideaustin.service.payment;

import java.io.IOException;
import java.io.StringWriter;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;

import org.joda.money.Money;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rideaustin.Constants;
import com.rideaustin.model.City;
import com.rideaustin.model.ride.ActiveDriver;
import com.rideaustin.model.ride.Ride;
import com.rideaustin.model.user.Driver;
import com.rideaustin.repo.dsl.ActiveDriverDslRepository;
import com.rideaustin.repo.dsl.DriverDslRepository;
import com.rideaustin.repo.dsl.RideDslRepository;
import com.rideaustin.rest.model.PeriodicReportType;
import com.rideaustin.service.model.DriverEarnings;
import com.rideaustin.service.model.DriverPayment;
import com.rideaustin.utils.DateUtils;

import au.com.bytecode.opencsv.CSVWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class EarningsReportingService {

  private final RideDslRepository rideDslRepository;
  private final DriverDslRepository driverDslRepository;
  private final ActiveDriverDslRepository activeDriverDslRepository;

  @Transactional
  public String getPaymentCsv(LocalDate reportDate, PeriodicReportType type, City city) throws IOException {
    LocalDate reportStartDate = reportDate.with(type.getStartAdjuster());
    LocalDate reportEndDate = reportStartDate.plus(type.getPeriod());
    Date reportStart = DateUtils.localDateToDate(reportStartDate, Constants.CST_ZONE);
    Date reportEnd = DateUtils.localDateToDate(reportEndDate, Constants.CST_ZONE);

    Map<Driver, DriverPayment> payments = collectPayments(reportStart, reportEnd, city);
    return writePaymentsCsv(reportDate, reportStartDate, payments, type);
  }

  @Nonnull
  @Transactional
  public List<DriverEarnings> collectEarnings(@Nullable LocalDate reportDate, @Nullable Long driver, City city) {
    LocalDate effectiveReportDate = reportDate == null ? LocalDate.now(Constants.CST_ZONE) : reportDate;
    LocalDate previousMonday = effectiveReportDate.with(TemporalAdjusters.previous(DayOfWeek.MONDAY));
    Date startDate = DateUtils.localDateToDate(previousMonday, Constants.CST_ZONE);
    Date endDate = DateUtils.localDateToDate(effectiveReportDate, Constants.CST_ZONE);

    Collection<Driver> rideFilterDrivers;
    if (driver != null) {
      rideFilterDrivers = Collections.singleton(driverDslRepository.findById(driver));
    } else {
      Date previousDay = DateUtils.localDateToDate(effectiveReportDate.minusDays(1), Constants.CST_ZONE);
      rideFilterDrivers = getDriverEarningsRides(previousDay, endDate, null, city)
        .map(Ride::getActiveDriver)
        .map(ActiveDriver::getDriver)
        .collect(Collectors.toList());
      log.debug("Found {} drivers with rides on {}", rideFilterDrivers.size(), previousDay);
    }

    Map<Driver, DriverEarnings> result = new HashMap<>();

    getDriverEarningsRides(startDate, endDate, rideFilterDrivers, city)
      .forEach(r -> result.computeIfAbsent(r.getActiveDriver().getDriver(),
        d -> new DriverEarnings(previousMonday, d, Constants.CST_ZONE))
        .addRide(r));

    Collection<Driver> resultDrivers = result.keySet();
    activeDriverDslRepository.findActiveDriversEndedBetween(startDate, endDate, resultDrivers)
      .forEach(ad -> result.get(ad.getDriver()).addActiveDriver(ad, startDate, endDate));

    return new ArrayList<>(result.values());
  }

  private Map<Driver, DriverPayment> collectPayments(Date reportStart, Date reportEnd, City city) {
    Map<Driver, DriverPayment> payments = new HashMap<>();
    getDriverEarningsRides(reportStart, reportEnd, null, city)
      .forEach(ride -> payments.computeIfAbsent(ride.getActiveDriver().getDriver(), d -> new DriverPayment())
        .addRidePayment(ride));
    return payments;
  }

  private String writePaymentsCsv(LocalDate reportDate, LocalDate reportStartDate,
    Map<Driver, DriverPayment> payments, PeriodicReportType type) throws IOException {
    StringWriter stringWriter = new StringWriter();
    try (CSVWriter writer = new CSVWriter(stringWriter)) {
      DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyyMMdd");
      String paymentIdPostfix = reportDate.format(dateFormat);
      String description = "Payment for " + type.getPeriodDescription() + reportStartDate.format(dateFormat);
      String currentDate = Constants.DATE_FORMATTER.format(LocalDate.now());
      payments.entrySet().stream()
        .sorted(Comparator.comparing(e -> e.getKey().getId()))
        .forEach(e -> {
          Driver driver = e.getKey();
          String driverId = driver.getPayoneerId() == null ? String.valueOf(driver.getId()) : driver.getPayoneerId();

          Money payment = e.getValue().getRidePayments();
          writer.writeNext(new String[]{
            driverId,
            payment.getAmount().toString(),
            payment.getCurrencyUnit().toString(),
            driverId + paymentIdPostfix,
            description,
            currentDate,
            driver.getFirstname(),
            driver.getLastname(),
            driver.getEmail()
          });
        });
    }
    return stringWriter.toString();
  }

  @Nonnull
  private Stream<Ride> getDriverEarningsRides(@Nonnull Date start, @Nonnull Date end,
    @Nullable Collection<Driver> drivers, City city) {

    Collection<Ride> earningsRides =
      rideDslRepository.getDriverEarnings(drivers, start.toInstant(), end.toInstant(), city);

    return earningsRides.stream();
  }

}
