package com.rideaustin.repo.dsl;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.querydsl.jpa.impl.JPAQuery;
import com.rideaustin.model.enums.RideUpgradeRequestStatus;
import com.rideaustin.model.ride.QRideUpgradeRequest;
import com.rideaustin.model.ride.RideUpgradeRequest;

@Repository
public class RideUpgradeRequestDslRepository extends AbstractDslRepository {

  private static final QRideUpgradeRequest qRequest = QRideUpgradeRequest.rideUpgradeRequest;

  public RideUpgradeRequest findByRiderAndStatus(long riderId, RideUpgradeRequestStatus... status) {
    return buildQuery(qRequest)
      .where(
        qRequest.requestedFrom.eq(riderId),
        qRequest.status.in(status)
      )
      .orderBy(qRequest.createdDate.desc())
      .fetchFirst();
  }

  public RideUpgradeRequest findByDriverAndStatus(long driverId, RideUpgradeRequestStatus... status) {
    return buildQuery(qRequest)
      .where(
        qRequest.requestedBy.eq(driverId),
        qRequest.status.in(status)
      )
      .orderBy(qRequest.createdDate.desc())
      .fetchFirst();
  }

  public List<RideUpgradeRequest> findExpired() {
    return buildQuery(qRequest)
      .where(
        qRequest.status.eq(RideUpgradeRequestStatus.REQUESTED),
        qRequest.expiresOn.before(new Date())
      )
      .fetch();
  }

  public boolean alreadyRequestedForRide(long driverId, long rideId) {
    return !createFindByRideAndDriverQuery(rideId, driverId)
      .fetch()
      .isEmpty();
  }

  public Optional<RideUpgradeRequest> findByRideAndDriver(long rideId, long driverId) {
    return Optional.ofNullable(
      createFindByRideAndDriverQuery(rideId, driverId)
        .fetchOne()
    );
  }

  private JPAQuery<RideUpgradeRequest> createFindByRideAndDriverQuery(long rideId, long driverId) {
    return buildQuery(qRequest)
      .where(
        qRequest.rideId.eq(rideId),
        qRequest.requestedBy.eq(driverId)
      );
  }
}
