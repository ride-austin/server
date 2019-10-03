package com.rideaustin.user.tracking.repo.dsl;

import static com.rideaustin.utils.SafeZeroUtils.safeZero;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import org.joda.money.Money;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Repository;

import com.querydsl.core.types.dsl.ComparablePath;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.rideaustin.model.ride.QRide;
import com.rideaustin.model.user.QRider;
import com.rideaustin.model.user.QUser;
import com.rideaustin.repo.dsl.AbstractDslRepository;
import com.rideaustin.rest.model.PagingParams;
import com.rideaustin.user.tracking.model.QUserTrack;
import com.rideaustin.user.tracking.model.QUserTrackStatsDto;
import com.rideaustin.user.tracking.model.UserTrackStatsDto;

@Repository
public class UserTrackDslRepository extends AbstractDslRepository {

  private static final QRide qRide = QRide.ride;
  private static final QUserTrack qUserTrack = QUserTrack.userTrack;
  private static final QRider qRider = QRider.rider;
  private static final QUser qUser = QUser.user;

  public Page<UserTrackStatsDto> findStatsForPeriod(Date start, Date end, PagingParams pagingParams) {
    final JPAQuery<UserTrackStatsDto> query = buildQuery(qRide)
      .leftJoin(qRide.rider, qRider)
      .leftJoin(qRider.user, qUser)
      .leftJoin(qUserTrack).on(qUser.id.eq(qUserTrack.userId))
      .select(new QUserTrackStatsDto(qUserTrack.utmSource, qUserTrack.utmCampaign, qUserTrack.utmMedium,
        qRide.id.count(), sum(qRide.fareDetails.totalFare), sum(qRide.fareDetails.driverPayment), sum(qRide.fareDetails.raPayment),
        qRide.distanceTravelled.sum().divide(1000L).castToNum(Double.class)))
      .where(
        qRide.createdDate.between(start, end),
        qUserTrack.isNotNull()
      )
      .groupBy(qUserTrack.utmSource, qUserTrack.utmCampaign, qUserTrack.utmMedium);
    final List<UserTrackStatsDto> content = query.fetch();
    return getPage(pagingParams, content, content.size());
  }

  private NumberExpression<BigDecimal> sum(ComparablePath<Money> money) {
    return safeZero(money).sum();
  }

}
