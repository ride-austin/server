package com.rideaustin.repo.dsl;

import java.util.Date;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Repository;

import com.google.common.collect.ImmutableList;
import com.rideaustin.model.enums.RideStatus;
import com.rideaustin.model.ride.QRide;
import com.rideaustin.model.ride.QRideQueueToken;
import com.rideaustin.model.ride.RideQueueToken;

@Repository
public class RideQueueTokenDslRepository extends AbstractDslRepository {

  private static final QRideQueueToken qRideQueueToken = QRideQueueToken.rideQueueToken;
  private static final QRide qRide = QRide.ride;

  @Scheduled(fixedDelay = 60000L)
  public void expire() {
    final List<Long> rideIds = buildQuery(qRideQueueToken)
      .select(qRideQueueToken.rideId)
      .where(
        qRideQueueToken.expired.isFalse(),
        qRideQueueToken.expiresOn.before(new Date())
      )
      .fetch();
    if (!rideIds.isEmpty()) {
      queryFactory.update(qRide)
        .set(ImmutableList.of(qRide.updatedDate, qRide.status), ImmutableList.of(new Date(), RideStatus.REQUEST_QUEUE_EXPIRED))
        .where(
          qRide.id.in(rideIds),
          qRide.status.eq(RideStatus.REQUEST_QUEUED)
        )
        .execute();
      queryFactory.update(qRideQueueToken)
        .set(qRideQueueToken.expired, true)
        .where(
          qRideQueueToken.expired.isFalse(),
          qRideQueueToken.expiresOn.before(new Date())
        )
        .execute();
    }
  }

  public RideQueueToken findOne(String token) {
    return buildQuery(qRideQueueToken)
      .where(qRideQueueToken.token.eq(token))
      .fetchOne();
  }
}
