package com.rideaustin.repo.dsl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Repository;

import com.querydsl.core.BooleanBuilder;
import com.rideaustin.model.user.QRider;
import com.rideaustin.model.user.QUser;
import com.rideaustin.report.entry.RidersExportReportEntry;
import com.rideaustin.rest.model.ListRidersParams;

@Repository
public class RiderReportDslRepository extends AbstractDslRepository {

  private static final QRider qRider = QRider.rider;

  public List<RidersExportReportEntry> exportRiders(ListRidersParams listRidersParams) {
    BooleanBuilder builder = new BooleanBuilder();
    listRidersParams.fill(builder);
    return queryFactory.select(
      qRider.id, qRider.user.email, qRider.user.phoneNumber, qRider.user.lastname, qRider.user.firstname, qRider.active,
      qRider.user.userEnabled)
      .from(qRider)
      .leftJoin(qRider.user, QUser.user)
      .where(builder)
      .orderBy(qRider.id.asc())
      .fetch()
      .stream()
      .map(RidersExportReportEntry::new)
      .collect(Collectors.toList());
  }
}
