package com.rideaustin.repo.dsl;

import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.summingDouble;
import static java.util.stream.Collectors.toMap;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.springframework.stereotype.Repository;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.NumberPath;
import com.rideaustin.Constants;
import com.rideaustin.model.ride.QActiveDriver;
import com.rideaustin.model.ride.QRide;
import com.rideaustin.model.user.QDriver;
import com.rideaustin.model.user.QRider;
import com.rideaustin.report.TupleConsumer;
import com.rideaustin.utils.DateUtils;

@Repository
public class UniqueRidersDriversReportRepository extends AbstractDslRepository implements TupleConsumer {

  private static final QDriver qDriver = QDriver.driver;
  private static final QRider qRider = QRider.rider;
  private static final QActiveDriver qActiveDriver = QActiveDriver.activeDriver;
  private static final QRide qRide = QRide.ride;

  public Map<LocalDate, Long> findDriversSignedUpByWeek() {
    return queryFactory.select(qDriver.agreementDate).from(qDriver).fetch().stream().collect(countByWeek());
  }

  public Map<LocalDate, Long> findFirstTimeDrivers() {
    return findFirstTimers(qRide.activeDriver.driver.id);
  }

  public Map<LocalDate, Long> findUniqueDrivers() {
    return findUnique(qRide.activeDriver.driver.id);
  }

  public Map<LocalDate, Long> findRidersSignedUpByWeek() {
    return queryFactory.select(qRider.user.createdDate).from(qRider).fetch().stream().collect(countByWeek());
  }

  public Map<LocalDate, Long> findFirstTimeRiders() {
    return findFirstTimers(qRide.rider.id);
  }

  public Map<LocalDate, Long> findUniqueRiders() {
    return findUnique(qRide.rider.id);
  }

  public Map<LocalDate, BigDecimal> sumOnlineHours() {
    List<Tuple> onlineHours = queryFactory.select(qActiveDriver.inactiveOn, qActiveDriver.createdDate)
      .from(qActiveDriver)
      .where(qActiveDriver.inactiveOn.isNotNull())
      .fetch();
    return sumHours(onlineHours);
  }

  public Map<LocalDate, BigDecimal> sumDrivenHours() {
    List<Tuple> drivenHours = queryFactory.select(qRide.completedOn, qRide.startedOn)
      .from(qRide)
      .where(
        qRide.completedOn.isNotNull()
          .and(qRide.startedOn.isNotNull())
          .and(qRide.startedOn.lt(qRide.completedOn))
      )
      .fetch();
    return sumHours(drivenHours);
  }

  private Map<LocalDate, BigDecimal> sumHours(List<Tuple> list) {
    return list
      .stream()
      .collect(
        groupingBy(
          t -> DateUtils.getEndOfWeek(getInstantFromTimestamp(t, 0)),
          summingDouble(t -> getInstantFromTimestamp(t, 0).getEpochSecond() - getInstantFromTimestamp(t, 1).getEpochSecond())
        )
      )
      .entrySet()
      .stream()
      .collect(
        toMap(
          Entry::getKey,
          e -> BigDecimal.valueOf(e.getValue()).divide(Constants.SECONDS_PER_HOUR, 2, Constants.ROUNDING_MODE)
        )
      );
  }

  private Map<LocalDate, Long> findFirstTimers(NumberPath<Long> id) {
    return queryFactory.select(qRide.createdDate.min()).from(qRide)
      .groupBy(id)
      .fetch()
      .stream()
      .collect(countByWeek());
  }

  private Map<LocalDate, Long> findUnique(NumberPath<Long> id) {
    return queryFactory.select(qRide.completedOn, id).from(qRide)
      .where(qRide.completedOn.isNotNull())
      .fetch()
      .stream()
      .map(CompletedRideInfo::new)
      .collect(Collectors.groupingBy(e -> DateUtils.getEndOfWeek(e.getCompletedOn()), Collectors.counting()));
  }

  private static Collector<Date, ?, Map<LocalDate, Long>> countByWeek() {
    return groupingBy(
      DateUtils::getEndOfWeek,
      counting()
    );
  }

  private static class CompletedRideInfo implements TupleConsumer {
    private final Instant completedOn;
    private final Long id;

    public CompletedRideInfo(Tuple tuple) {
      this.completedOn = getInstantFromTimestamp(tuple, 0);
      this.id = getLong(tuple, 1);
    }

    public Instant getCompletedOn() {
      return completedOn;
    }

    public Long getId() {
      return id;
    }
  }
}
