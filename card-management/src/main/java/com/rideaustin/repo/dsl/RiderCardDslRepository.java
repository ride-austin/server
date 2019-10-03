package com.rideaustin.repo.dsl;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.rideaustin.model.user.QRiderCard;
import com.rideaustin.model.user.Rider;
import com.rideaustin.model.user.RiderCard;

@Repository
public class RiderCardDslRepository extends AbstractDslRepository {

  private static final QRiderCard qRiderCard = QRiderCard.riderCard;

  public List<RiderCard> findByRider(Rider rider) {
    return buildQuery(qRiderCard)
      .where(qRiderCard.rider.eq(rider)
        .and(qRiderCard.removed.eq(Boolean.FALSE)))
      .fetch();
  }

  public RiderCard findOne(Long id) {
    return get(id, RiderCard.class);
  }

  public List<String> findFingerprintsByRider(Rider rider) {
    return buildQuery(qRiderCard)
      .select(qRiderCard.fingerprint)
      .where(qRiderCard.rider.eq(rider)
        .and(qRiderCard.removed.eq(Boolean.FALSE)))
      .fetch();
  }
}
