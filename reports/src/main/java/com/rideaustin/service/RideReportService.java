package com.rideaustin.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.inject.Inject;

import org.joda.money.Money;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import com.google.common.collect.ImmutableList;
import com.querydsl.core.Tuple;
import com.rideaustin.Constants;
import com.rideaustin.model.enums.RideStatus;
import com.rideaustin.model.ride.Ride;
import com.rideaustin.repo.dsl.RideDslRepository;
import com.rideaustin.repo.dsl.RideReportDslRepository;
import com.rideaustin.repo.jpa.DriverRidesReportRepository;
import com.rideaustin.rest.model.ListRidesParams;
import com.rideaustin.rest.model.PagingParams;
import com.rideaustin.service.model.CumulativeRidesReportEntry;
import com.rideaustin.service.model.DriverRidesReportEntry;
import com.rideaustin.service.model.RideReportEntry;
import com.rideaustin.utils.DateUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class RideReportService {

  private static final EnumSet<RideStatus> CANCELLED_STATUSES = EnumSet.of(
    RideStatus.ADMIN_CANCELLED,
    RideStatus.DRIVER_CANCELLED,
    RideStatus.RIDER_CANCELLED
  );

  private final RideReportDslRepository rideReportDslRepository;
  private final DriverRidesReportRepository driverRidesReportRepository;
  private final RideDslRepository rideDslRepository;


  public Page<Tuple> getRidesZipCodeReport(Instant completedOnAfter, Instant completedOnBefore, String zipCode, PagingParams pagingParams) {
    return rideReportDslRepository.getZipCodeReport(Date.from(completedOnAfter), Date.from(completedOnBefore), zipCode, pagingParams);
  }

  public CumulativeRidesReportEntry getCumulativeRidesReport(Instant completedOnAfter, Instant completedOnBefore, String zipCode, Long cityId, PagingParams pagingParams) {
    CumulativeRidesReportEntry report = new CumulativeRidesReportEntry();
    report.setDriversRidesReport(getRidesByUsersReport(completedOnAfter, completedOnBefore, zipCode, cityId, pagingParams));
    report.setRidesReport(getRidesReport(completedOnAfter, completedOnBefore, zipCode, cityId, null));
    return report;
  }

  public List<RideReportEntry> getRidesReport(Instant completedOnAfter, Instant completedOnBefore, String zipCode, Long cityId, String zoneOffset) {

    ListRidesParams params = new ListRidesParams();

    params.setStatus(ImmutableList.<RideStatus>builder().add(RideStatus.COMPLETED).addAll(CANCELLED_STATUSES).build());
    params.setCompletedOnAfter(completedOnAfter);
    params.setCompletedOnBefore(completedOnBefore);
    params.setZipCode(zipCode);
    params.setCityId(cityId);
    List<Ride> rides = rideDslRepository.ridesList(params);

    Map<Date, List<Ride>> mapOfDailyRidesWithTimezone = groupByDatesUsingTimeZone(zoneOffset, rides);

    return calculateDailyResults(mapOfDailyRidesWithTimezone);
  }

  public Page<DriverRidesReportEntry> getRidesByUsersReport(Instant completedOnAfter, Instant createdOnBefore, String zipCode, Long cityId, PagingParams pagingParams) {
    return driverRidesReportRepository.driverRideReport(Date.from(completedOnAfter), Date.from(createdOnBefore), zipCode, cityId, pagingParams);
  }

  private List<RideReportEntry> calculateDailyResults(Map<Date, List<Ride>> mapOfDailyRidesWithTimezone) {
    List<RideReportEntry> resultsList = new ArrayList<>();
    for (Map.Entry<Date, List<Ride>> dailyRidesEntry : mapOfDailyRidesWithTimezone.entrySet()) {
      List<Ride> groupedRides = dailyRidesEntry.getValue();
      RideReportEntry entry = new RideReportEntry(dailyRidesEntry.getKey(), groupedRides.stream().filter(r -> r.getStatus().equals(RideStatus.COMPLETED)).count());
      for (Ride r : groupedRides) {
        entry.setDistanceTraveled(entry.getDistanceTraveled().add(Optional.ofNullable(r.getDistanceTravelled()).orElse(BigDecimal.ZERO)));
        entry.setTotalFares(entry.getTotalFares().add(Optional.ofNullable(r.getTotalFare()).map(Money::getAmount).orElse(BigDecimal.ZERO)));
        if (r.getStatus().equals(RideStatus.COMPLETED) && r.getSurgeFactor().compareTo(BigDecimal.ONE) > 0) {
          entry.setPriorityFaresRidesCount(entry.getPriorityFaresRidesCount() + 1);
        }
        if (CANCELLED_STATUSES.contains(r.getStatus())) {
          entry.setCancelledRidesCount(entry.getCancelledRidesCount() + 1);
        }
      }
      BigDecimal averageDistanceTraveled = BigDecimal.ZERO;
      double averageTotalFares = 0.0;
      if (entry.getRidesCount() > 0) {
        averageDistanceTraveled = entry.getDistanceTraveled().divide(BigDecimal.valueOf(entry.getRidesCount()), Constants.ROUNDING_MODE);
        averageTotalFares = entry.getTotalFares().divide(BigDecimal.valueOf(entry.getRidesCount()), Constants.ROUNDING_MODE).doubleValue();
      }
      entry.setAverageDistanceTraveled(averageDistanceTraveled);
      entry.setAverageTotalFares(averageTotalFares);
      resultsList.add(entry);
    }
    return resultsList;
  }

  private Map<Date, List<Ride>> groupByDatesUsingTimeZone(String zoneOffset, List<Ride> rides) {
    Map<Date, List<Ride>> mapOfDailyRidesWithTimezone = new HashMap<>();
    for (Ride ride : rides) {
      Date endDate = Optional.ofNullable(ride.getCompletedOn()).orElse(ride.getCancelledOn());
      Date zonedDate = DateUtils.getDateStringWithOffset(endDate, zoneOffset);
      mapOfDailyRidesWithTimezone.putIfAbsent(zonedDate, new ArrayList<>());
      mapOfDailyRidesWithTimezone.get(zonedDate).add(ride);
    }
    return mapOfDailyRidesWithTimezone;
  }
}
