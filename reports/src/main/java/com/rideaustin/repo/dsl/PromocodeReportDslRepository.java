package com.rideaustin.repo.dsl;

import static java.util.stream.Collectors.toList;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.rideaustin.model.reports.QPromocodeUsageReportEntry;
import com.rideaustin.report.entry.PromocodeUsageReportEntry;

@Repository
public class PromocodeReportDslRepository extends AbstractDslRepository {

  private static final QPromocodeUsageReportEntry qReportEntry = QPromocodeUsageReportEntry.promocodeUsageReportEntry;

  public List<PromocodeUsageReportEntry> getPromocodeUsageReport(String codeLiteral, boolean completedOnly) {
    return queryFactory.select(qReportEntry.id.riderId, qReportEntry.firstName, qReportEntry.lastName, qReportEntry.email,
      qReportEntry.completedOn.min(), qReportEntry.id.rideId.count())
      .from(qReportEntry)
      .where(qReportEntry.codeLiteral.eq(codeLiteral).and(completedRidesPredicate(completedOnly)))
      .groupBy(qReportEntry.id.riderId)
      .orderBy(qReportEntry.id.rideId.count().desc(), qReportEntry.id.riderId.asc())
      .fetch()
      .stream()
      .map(PromocodeUsageReportEntry::new)
      .collect(toList());
  }

  private BooleanExpression completedRidesPredicate(boolean completedOnly) {
    return completedOnly ? qReportEntry.id.rideId.isNotNull() : null;
  }

}
