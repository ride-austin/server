package com.rideaustin.repo.dsl;

import java.sql.Date;
import java.time.Instant;

import javax.inject.Inject;

import org.springframework.core.env.Environment;
import org.springframework.stereotype.Repository;

import com.rideaustin.model.QRideCall;
import com.rideaustin.model.RideCall;
import com.rideaustin.model.RideCallType;

@Repository
public class RideCallDslRepository extends AbstractDslRepository {

  private final long pendingCallTimeThreshold;
  private static final QRideCall qRideCall = QRideCall.rideCall;

  @Inject
  public RideCallDslRepository(Environment environment) {
    pendingCallTimeThreshold = environment.getProperty("call.twilio.pending.time.threshold", Long.class, 30L);
  }

  public RideCall findBySid(String sid) {
    return buildQuery(qRideCall)
      .where(qRideCall.callSid.eq(sid))
      .fetchOne();
  }

  public boolean hasUnprocessedCalls(long rideId) {
    return buildQuery(qRideCall)
      .where(
        qRideCall.rideId.eq(rideId)
          .and(qRideCall.processed.isFalse())
          .and(qRideCall.type.eq(RideCallType.CALL))
          .and(qRideCall.updatedDate.goe(Date.from(Instant.now().minusSeconds(pendingCallTimeThreshold))))
      ).fetchCount() > 0;
  }
}
