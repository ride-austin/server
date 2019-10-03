package com.rideaustin.repo.dsl;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.rideaustin.model.ride.QRideTracker;
import com.rideaustin.model.ride.Ride;
import com.rideaustin.model.ride.RideTracker;

@Repository
public class RideTrackerDslRepository extends AbstractDslRepository {

  private static QRideTracker qRideTracker = QRideTracker.rideTracker;

  public List<RideTracker> findAllTrackerRecord(Long rideId) {
    return buildQuery(qRideTracker)
      .where(qRideTracker.rideId.eq(rideId))
      .orderBy(qRideTracker.sequence.asc())
      .fetch();
  }

  public RideTracker findLastRecord(Long rideId) {
    return buildQuery(qRideTracker)
      .where(qRideTracker.rideId.eq(rideId))
      .orderBy(qRideTracker.sequence.desc())
      .fetchFirst();
  }

  public List<RideTracker> findValidTrackerRecord(Ride ride) {
    return buildQuery(qRideTracker)
      .where(qRideTracker.rideId.eq(ride.getId()).and(qRideTracker.valid.isTrue()))
      .orderBy(qRideTracker.sequence.asc()).fetch();
  }
}
