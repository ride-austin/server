package com.rideaustin.service.model;

import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.AbstractMap;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.joda.money.Money;

import com.rideaustin.Constants;
import com.rideaustin.model.enums.ActiveDriverStatus;
import com.rideaustin.model.ride.ActiveDriver;
import com.rideaustin.model.ride.Ride;
import com.rideaustin.model.user.Driver;
import com.rideaustin.utils.DateUtils;

public class DriverEarnings {

  private final LocalDate reportDate;
  private final Driver driver;
  private final ZoneId zoneId;
  private final Map<LocalDate, DriverDailyEarnings> dailyEarnings = new HashMap<>();
  private long secondsOnline = 0L;

  public DriverEarnings(LocalDate reportDate, Driver driver, ZoneId zoneId) {
    this.reportDate = reportDate;
    this.driver = driver;
    this.zoneId = zoneId;
  }

  public void addRide(Ride ride) {
    LocalDate rideDate;
    if (ride.getCompletedOn() != null) {
      rideDate = DateUtils.dateToInstant(ride.getCompletedOn()).atZone(zoneId).toLocalDate();
    } else {
      rideDate = DateUtils.dateToInstant(ride.getCreatedDate()).atZone(zoneId).toLocalDate();
    }
    dailyEarnings.computeIfAbsent(rideDate, d -> new DriverDailyEarnings()).addRide(ride);
  }

  public void addActiveDriver(ActiveDriver ad, Date start, Date end) {
    Date addStartDate = ad.getCreatedDate();
    Date addEndDate = ad.getUpdatedDate();
    if (ActiveDriverStatus.INACTIVE.equals(ad.getStatus())) {
      addEndDate = ad.getInactiveOn();
    }
    if (addStartDate.before(start)) {
      addStartDate = start;
    }
    if (addEndDate.after(end)) {
      addEndDate = end;
    }
    secondsOnline += Duration.between(DateUtils.dateToInstant(addStartDate),
      DateUtils.dateToInstant(addEndDate)).getSeconds();
  }

  public LocalDate getReportLocalDate() {
    return reportDate;
  }

  public Date getReportDate() {
    return DateUtils.localDateToDate(reportDate, zoneId);
  }

  public Driver getDriver() {
    return driver;
  }

  public List<Entry<Date, DriverDailyEarnings>> getDailyEarnings() {
    return dailyEarnings.entrySet().stream()
      .sorted(Comparator.comparing(Entry::getKey))
      .map(e -> new AbstractMap.SimpleEntry<>(DateUtils.localDateToDate(e.getKey(), zoneId), e.getValue()))
      .collect(Collectors.toList());
  }

  private Stream<DriverDailyEarnings> getDailyEarningsStream() {
    return dailyEarnings.values().stream();
  }

  public Money getTotalEarnings() {
    return getDailyEarningsStream()
      .map(DriverDailyEarnings::getEarning)
      .reduce(Constants.ZERO_USD, Money::plus);
  }

  public int getTotalRides() {
    return getDailyEarningsStream()
      .mapToInt(DriverDailyEarnings::getRideCount)
      .sum();
  }

  public String getHoursOnline() {
    return String.format("%02d:%02d:%02d", secondsOnline / 3600, (secondsOnline % 3600) / 60, secondsOnline % 60);
  }

}
