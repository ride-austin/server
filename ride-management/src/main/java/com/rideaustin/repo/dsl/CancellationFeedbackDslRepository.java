package com.rideaustin.repo.dsl;

import org.springframework.stereotype.Repository;

import com.rideaustin.model.ride.QRideCancellationFeedback;

@Repository
public class CancellationFeedbackDslRepository extends AbstractDslRepository {

  private static final QRideCancellationFeedback qFeedback = QRideCancellationFeedback.rideCancellationFeedback;

  public boolean noFeedbackYet(long rideId, long userId) {
    return buildQuery(qFeedback)
      .where(
        qFeedback.rideId.eq(rideId),
        qFeedback.submittedBy.eq(userId)
      )
      .fetch()
      .isEmpty();
  }
}
