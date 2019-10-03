package com.rideaustin.repo.dsl;

import java.util.Date;

import org.joda.time.DateTime;
import org.springframework.stereotype.Repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.JPAExpressions;
import com.rideaustin.model.user.DriverAudited;
import com.rideaustin.model.user.QDriverAudited;

@Repository
public class DriverAuditedDslRepository extends AbstractDslRepository {
  private static final QDriverAudited qDriverAudited = QDriverAudited.driverAudited;

  public DriverAudited getLastDriverAudited(long driverId) {
    BooleanBuilder query = new BooleanBuilder();
    query.and(qDriverAudited.id.eq(driverId));
    query.and(qDriverAudited.revision.eq(JPAExpressions.select(qDriverAudited.revision.max()).from(qDriverAudited)));
    return buildQuery(qDriverAudited).where(query).fetchOne();
  }

  public Iterable<DriverAudited> findByDayAndDriverId(Long driverId, Date day) {
    BooleanBuilder query = new BooleanBuilder();
    query.and(qDriverAudited.revisionDate.between(
      new DateTime(day).withTimeAtStartOfDay().toDate(),
      new DateTime(day).plusDays(1).withTimeAtStartOfDay().toDate()));
    query.and(qDriverAudited.id.eq(driverId));
    return buildQuery(qDriverAudited).where(query).orderBy(qDriverAudited.revision.asc()).fetch();
  }

  public DriverAudited findEarlierRevision(long driverId, long lastRevisionNr) {
    BooleanBuilder query = new BooleanBuilder();
    query.and(qDriverAudited.id.eq(driverId));
    query.and(qDriverAudited.revision.eq(JPAExpressions.select(qDriverAudited.revision.max()).from(qDriverAudited)
      .where(qDriverAudited.id.eq(driverId).and(qDriverAudited.revision.lt(lastRevisionNr)))));
    return buildQuery(qDriverAudited).where(query).fetchOne();
  }
}
