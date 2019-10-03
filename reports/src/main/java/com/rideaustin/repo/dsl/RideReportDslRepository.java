package com.rideaustin.repo.dsl;

import static com.rideaustin.utils.SafeZeroUtils.safeZero;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.annotation.Nonnull;

import org.apache.commons.lang3.StringUtils;
import org.joda.money.Money;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.ComparablePath;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.jpa.impl.JPAQuery;
import com.rideaustin.model.enums.RideStatus;
import com.rideaustin.model.reports.AvatarTripCountReportResultEntry;
import com.rideaustin.model.reports.TNCTripReportResult;
import com.rideaustin.model.reports.TripFulfillmentQueryResultEntry;
import com.rideaustin.model.reports.WeeklyStatisticsReportResultEntry;
import com.rideaustin.model.ride.QActiveDriver;
import com.rideaustin.model.ride.QCarType;
import com.rideaustin.model.ride.QCityCarType;
import com.rideaustin.model.ride.QRide;
import com.rideaustin.model.surgepricing.QSurgeAreaHistory;
import com.rideaustin.model.user.QUser;
import com.rideaustin.report.TupleConsumer;
import com.rideaustin.report.entry.RiderTotalTripCountReportEntry;
import com.rideaustin.report.entry.RidesExportReportEntry;
import com.rideaustin.report.entry.SurgeHistoryEntry;
import com.rideaustin.report.entry.TNCDriverStatsReportEntry;
import com.rideaustin.report.entry.TNCSummaryReportEntry;
import com.rideaustin.rest.model.ListRidesParams;
import com.rideaustin.rest.model.PagingParams;

@Repository
public class RideReportDslRepository extends AbstractDslRepository implements TupleConsumer {

  private static final QRide qRide = QRide.ride;

  public List<Date> getRideStartByDateAndLocation(
    Double startedLatFrom, Double startedLngFrom,
    Double startedLatTo, Double startedLngTo,
    Date startedOnAfter, Date startedOnBefore) {

    return queryFactory.select(qRide.startedOn).from(qRide)
      .where(qRide.startLocationLat.between(startedLatFrom, startedLatTo)
        .and(qRide.startLocationLong.between(startedLngFrom, startedLngTo))
        .and(qRide.startedOn.between(startedOnAfter, startedOnBefore))
        .and(qRide.status.eq(RideStatus.COMPLETED)))
      .fetch();
  }

  public Page<Tuple> getZipCodeReport(@Nonnull Date completedOnAfter, @Nonnull Date completedOnBefore, String zipCode, PagingParams pagingParams) {
    BooleanBuilder where = new BooleanBuilder();
    addDatesConditions(completedOnAfter, completedOnBefore, where);
    addZipCodeConditions(zipCode, where);

    JPAQuery<Tuple> query = queryFactory.select(qRide.start.zipCode.as("zipCode"),
      qRide.start.zipCode.count().as("rideCount"))
      .from(qRide)
      .where(where)
      .groupBy(qRide.start.zipCode);

    List<Tuple> content = query.fetch();
    if (pagingParams != null) {
      appendPagingParams(query, pagingParams, qRide, "rideCount");
      long total = query.fetchCount();
      return new PageImpl<>(content, pagingParams.toPageRequest(), total);
    } else {
      return new PageImpl<>(content, new PageRequest(1, content.size()), content.size());
    }
  }

  public List<TripFulfillmentQueryResultEntry> getTripFulfillmentReport(@Nonnull Instant startDateTime, @Nonnull Instant endDateTime) {
    Date start = Date.from(startDateTime);
    Date end = Date.from(endDateTime);
    BooleanBuilder where = new BooleanBuilder();
    where.and(qRide.status.eq(RideStatus.COMPLETED)
      .and(qRide.completedOn.between(start, end))
    ).or(qRide.status.in(RideStatus.ADMIN_CANCELLED, RideStatus.DRIVER_CANCELLED, RideStatus.RIDER_CANCELLED)
      .and(qRide.cancelledOn.between(start, end))
    ).or(qRide.status.eq(RideStatus.NO_AVAILABLE_DRIVER)
      .and(qRide.createdDate.between(start, end)));

    return queryFactory.select(qRide.createdDate, qRide.completedOn, qRide.cancelledOn, qRide.status)
      .from(qRide)
      .where(where)
      .fetch()
      .stream()
      .map(TripFulfillmentQueryResultEntry::new)
      .collect(toList());
  }

  public Set<TNCTripReportResult> getTNCTripReportResultSet(@Nonnull Instant startDate, @Nonnull Instant endDate) {
    Date start = Date.from(startDate);
    Date end = Date.from(endDate);
    BooleanBuilder where = new BooleanBuilder()
      .and(qRide.createdDate.between(start, end));

    return queryFactory.select(qRide.createdDate, qRide.status, qRide.start.zipCode, qRide.end.zipCode, qRide.completedOn).from(qRide)
      .where(where)
      .fetch()
      .stream()
      .map(TNCTripReportResult::new)
      .collect(toSet());
  }

  public TNCSummaryReportEntry getTNCSummary(@Nonnull Instant startDate, @Nonnull Instant endDate) {
    Date start = Date.from(startDate);
    Date end = Date.from(endDate);
    AggregateExpressions expr = new AggregateExpressions().invoke();

    return new TNCSummaryReportEntry(queryFactory.select(qRide.id.count(), qRide.rider.id.countDistinct(),
      sum(qRide.fareDetails.totalFare),
      expr.hoursDriven,
      qRide.distanceTravelled.sum())
      .from(qRide)
      .where(qRide.status.eq(RideStatus.COMPLETED)
        .and(qRide.createdDate.between(start, end))
        .and(expr.secondsDrivenEach.gt(0)))
      .fetchOne());
  }

  public List<SurgeHistoryEntry> getSurgeAreaHistory(@Nonnull Instant startDate, @Nonnull Instant endDate) {
    Date start = Date.from(startDate);
    Date end = Date.from(endDate);
    QSurgeAreaHistory qSurgeAreaHistory = QSurgeAreaHistory.surgeAreaHistory;
    return queryFactory.select(qSurgeAreaHistory.createdDate, qSurgeAreaHistory.surgeFactor).from(qSurgeAreaHistory)
      .where(qSurgeAreaHistory.createdDate.between(start, end))
      .orderBy(qSurgeAreaHistory.name.asc(), qSurgeAreaHistory.createdDate.asc())
      .fetch()
      .stream()
      .map(t -> new SurgeHistoryEntry(getInstantFromTimestamp(t, 0), getBigDecimal(t, 1)))
      .collect(toList());
  }

  public List<RidesExportReportEntry> exportRides(ListRidesParams params) {
    BooleanBuilder builder = new BooleanBuilder();
    params.fill(builder);
    QUser riderUser = QUser.user;
    QUser driverUser = new QUser("driverUser");
    return queryFactory.select(qRide.id, qRide.rider.id, qRide.activeDriver.driver.id, qRide.activeDriver.id, qRide.rider.user.firstname,
      qRide.rider.user.lastname, qRide.rider.user.email, qRide.activeDriver.driver.user.firstname,
      qRide.activeDriver.driver.user.lastname, qRide.activeDriver.driver.user.email, qRide.createdDate,
      qRide.startLocationLat, qRide.startLocationLong, qRide.completedOn, qRide.endLocationLat, qRide.endLocationLong,
      qRide.distanceTravelled, qRide.fareDetails.baseFare, qRide.fareDetails.distanceFare, qRide.fareDetails.timeFare,
      qRide.fareDetails.subTotal, qRide.fareDetails.bookingFee, qRide.fareDetails.cityFee, qRide.fareDetails.totalFare,
      qRide.fareDetails.driverPayment, qRide.tippedOn, qRide.fareDetails.tip)
      .from(qRide)
      .leftJoin(qRide.activeDriver.driver.user, driverUser)
      .leftJoin(qRide.rider.user, riderUser)
      .where(builder)
      .orderBy(qRide.id.asc())
      .fetch()
      .stream()
      .map(RidesExportReportEntry::new)
      .collect(toList());
  }

  public List<TNCDriverStatsReportEntry> getDriversStatsReport(Instant startDate, Instant endDate) {
    Date start = Date.from(startDate);
    Date end = Date.from(endDate);
    AggregateExpressions expr = new AggregateExpressions().invoke();
    return queryFactory.select(
      qRide.activeDriver.driver.id,
      qRide.activeDriver.driver.user.firstname,
      qRide.activeDriver.driver.user.lastname,
      expr.hoursDriven,
      qRide.distanceTravelled.sum()
    )
      .from(qRide)
      .innerJoin(qRide.activeDriver, QActiveDriver.activeDriver)
      .where(qRide.status.eq(RideStatus.COMPLETED)
        .and(qRide.createdDate.between(start, end)))
      .groupBy(qRide.activeDriver.driver.id)
      .orderBy(qRide.activeDriver.driver.id.asc())
      .fetch(
      )
      .stream()
      .map(TNCDriverStatsReportEntry::new)
      .collect(toList());
  }

  public List<RiderTotalTripCountReportEntry> getRiderTripTotalCounts(Instant completedOnBefore) {
    return queryFactory.select(
      qRide.rider.id, qRide.rider.user.firstname, qRide.rider.user.lastname, qRide.rider.user.email, qRide.startedOn.min(), qRide.id.count())
      .from(qRide)
      .where(qRide.status.eq(RideStatus.COMPLETED).and(qRide.completedOn.loe(Date.from(completedOnBefore))))
      .groupBy(qRide.rider.id)
      .orderBy(qRide.id.count().desc())
      .fetch()
      .stream()
      .map(RiderTotalTripCountReportEntry::new)
      .collect(toList());
  }

  public List<WeeklyStatisticsReportResultEntry> getWeeklyStatisticsRaw(Instant completedOnBefore) {
    QCityCarType qCityCarType = QCityCarType.cityCarType;
    QCarType qCarType = QCarType.carType;
    return queryFactory.select(
      qRide.completedOn, qRide.requestedCarType.carCategory, qRide.fareDetails.surgeFare, qRide.surgeFactor,
      qRide.fareDetails.subTotal, qRide.fareDetails.bookingFee, qRide.fareDetails.cityFee, qRide.fareDetails.normalFare,
      qRide.fareDetails.driverPayment, qCityCarType.fixedRAFee, qRide.fareDetails.cancellationFee, qRide.fareDetails.tip,
      qRide.fareDetails.roundUpAmount, qRide.distanceTravelled
    ).from(qRide, qCarType, qCityCarType)
      .innerJoin(qRide.requestedCarType, qCarType)
      .innerJoin(qCityCarType.carType, qCarType)
      .where(
        qRide.status.eq(RideStatus.COMPLETED),
        qRide.completedOn.before(Date.from(completedOnBefore)),
        qCityCarType.enabled.isTrue(),
        qCityCarType.cityId.eq(qRide.cityId),
        qCityCarType.carType.eq(qRide.requestedCarType)
      )
      .fetch()
      .stream()
      .map(WeeklyStatisticsReportResultEntry::new)
      .collect(toList());
  }

  public List<AvatarTripCountReportResultEntry> getDriverTripsRaw(Instant completedOnBefore) {
    return getAvatarTripsRaw(completedOnBefore, qRide.activeDriver.driver.id, qRide.activeDriver.driver.user);
  }

  public List<AvatarTripCountReportResultEntry> getRiderTripsRaw(Instant completedOnBefore) {
    return getAvatarTripsRaw(completedOnBefore, qRide.rider.id, qRide.rider.user);
  }

  private List<AvatarTripCountReportResultEntry> getAvatarTripsRaw(Instant completedOnBefore, NumberPath<Long> id, QUser user) {
    return queryFactory.select(
      qRide.id, id, qRide.completedOn, user.firstname, user.lastname, user.email
    ).from(qRide)
      .where(
        qRide.status.eq(RideStatus.COMPLETED),
        qRide.completedOn.before(Date.from(completedOnBefore))
      )
      .fetch()
      .stream()
      .map(AvatarTripCountReportResultEntry::new)
      .collect(toList());
  }

  private void addDatesConditions(Date completedOnAfter, Date completedOnBefore, BooleanBuilder where) {
    where.and(qRide.completedOn.between(completedOnAfter, completedOnBefore)
      .and(qRide.status.eq(RideStatus.COMPLETED)));
  }

  private void addZipCodeConditions(String zipCode, BooleanBuilder where) {
    if (StringUtils.isNotEmpty(zipCode)) {
      where.and(qRide.start.zipCode.eq(zipCode)
        .or(qRide.end.zipCode.eq(zipCode)));
    }
  }

  private NumberExpression<BigDecimal> sum(ComparablePath<Money> money) {
    return safeZero(money).sum();
  }

  private class AggregateExpressions {
    private static final String TIMEDIFF = "unix_timestamp({0}) - unix_timestamp({1})";
    private NumberExpression<BigDecimal> hoursDriven;
    private NumberExpression<BigDecimal> secondsDrivenEach;

    AggregateExpressions invoke() {
      hoursDriven = Expressions.numberTemplate(BigDecimal.class,
        String.format("sum(%s)", TIMEDIFF), qRide.completedOn, qRide.startedOn);
      secondsDrivenEach = Expressions.numberTemplate(BigDecimal.class,
        TIMEDIFF, qRide.completedOn, qRide.startedOn);
      return this;
    }
  }
}
