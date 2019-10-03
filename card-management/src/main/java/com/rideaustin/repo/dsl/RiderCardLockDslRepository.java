package com.rideaustin.repo.dsl;

import java.util.Collection;
import java.util.List;

import org.springframework.stereotype.Repository;

import com.rideaustin.model.ride.Ride;
import com.rideaustin.model.user.QRiderCardLock;
import com.rideaustin.model.user.RiderCardLock;

@Repository
public class RiderCardLockDslRepository extends AbstractDslRepository {

  private static final QRiderCardLock qRiderCardLock = QRiderCardLock.riderCardLock;

  public RiderCardLock findByFingerprint(String fingerprint) {
    return buildQuery(qRiderCardLock)
      .where(qRiderCardLock.cardFingerprint.eq(fingerprint))
      .orderBy(qRiderCardLock.id.asc())
      .fetchFirst();
  }

  public List<RiderCardLock> findByFingerprints(Collection<String> fingerprints) {
    return buildQuery(qRiderCardLock)
      .where(qRiderCardLock.cardFingerprint.in(fingerprints))
      .fetch();
  }

  public RiderCardLock findByFingerprintAndRide(String fingerprint, Ride ride) {
    return buildQuery(qRiderCardLock)
      .where(qRiderCardLock.cardFingerprint.eq(fingerprint).and(qRiderCardLock.ride.eq(ride)))
      .fetchOne();
  }

  public List<RiderCardLock> findByRide(Ride ride) {
    return buildQuery(qRiderCardLock)
      .where(qRiderCardLock.ride.eq(ride))
      .fetch();
  }
}
