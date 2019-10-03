package com.rideaustin.repo.dsl;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.rideaustin.model.GeolocationLog;
import com.rideaustin.model.QGeolocationLog;
import com.rideaustin.model.enums.GeolocationLogEvent;
import com.rideaustin.model.enums.RideStatus;
import com.rideaustin.model.ride.QRide;
import com.rideaustin.model.user.Rider;

/**
 * defines the database access methods of the geolocation log entity
 */
@Repository
public class GeolocationLogDslRepository extends AbstractDslRepository {

  private static QGeolocationLog qGeolocationLog = QGeolocationLog.geolocationLog;
  private static QRide qRide = QRide.ride;

  public List<GeolocationLog> findBetweenDatesWithEvent(Date beginDate, Date endDate, GeolocationLogEvent event) {
    BooleanExpression where = qGeolocationLog.createdDate.between(beginDate, endDate)
      .and(qGeolocationLog.event.eq(event));
    if (event == GeolocationLogEvent.RIDER_APP_OPEN) {
      where.and(qGeolocationLog.rider.notIn(
        JPAExpressions.select(qRide.rider).from(qRide)
          .where(qRide.status.in(RideStatus.ONGOING_RIDER_STATUSES))
      ));
    }
    where = where.and(qGeolocationLog.carType.isNotNull());
    return buildQuery(qGeolocationLog)
      .where(where)
      .orderBy(qGeolocationLog.id.desc())
      .fetch();
  }

  public Set<Rider> findRecentUsers(Date fromDate) {
    List<Rider> riders = buildQuery(qGeolocationLog)
      .select(qGeolocationLog.rider)
      .distinct()
      .where(qGeolocationLog.createdDate.after(fromDate)
        .and(qGeolocationLog.event.eq(GeolocationLogEvent.GET_ACTIVE_DRIVERS_BY_RIDER)))
      .fetch();
    if (riders == null) {
      return new HashSet<>();
    } else {
      return new HashSet<>(riders);
    }
  }
}
