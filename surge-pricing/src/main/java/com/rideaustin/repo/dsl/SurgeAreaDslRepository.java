package com.rideaustin.repo.dsl;

import java.util.Date;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQuery;
import com.rideaustin.model.enums.RideStatus;
import com.rideaustin.model.ride.QRide;
import com.rideaustin.model.ride.Ride;
import com.rideaustin.model.surgepricing.AreaGeometry;
import com.rideaustin.model.surgepricing.QSurgeArea;
import com.rideaustin.model.surgepricing.QSurgeAreaHistory;
import com.rideaustin.model.surgepricing.SurgeArea;
import com.rideaustin.model.surgepricing.SurgeAreaHistory;

@Repository
public class SurgeAreaDslRepository extends AbstractDslRepository {

  private static final QSurgeArea qSurgeArea = QSurgeArea.surgeArea;
  private static final QRide qRide = QRide.ride;
  private static final QSurgeAreaHistory qSurgeAreaHistory = QSurgeAreaHistory.surgeAreaHistory;

  public List<SurgeArea> findAllActive() {
    return buildQuery(qSurgeArea)
      .where(qSurgeArea.active.isTrue())
      .fetch();
  }

  public SurgeAreaHistory findLatestHistoryItem(SurgeArea surgeArea, String surgeFactorCarCategory) {
    return buildQuery(qSurgeAreaHistory)
      .where(
        qSurgeAreaHistory.surgeAreaId.eq(surgeArea.getId()),
        qSurgeAreaHistory.surgeFactorCarCategory.eq(surgeFactorCarCategory)
      )
      .orderBy(qSurgeAreaHistory.id.desc()).limit(1)
      .fetchOne();
  }

  public SurgeArea findOne(Long surgeAreaId) {
    return buildQuery(qSurgeArea)
      .where(
        qSurgeArea.id.eq(surgeAreaId)
          .and(qSurgeArea.active.isTrue())
      )
      .fetchOne();
  }

  public SurgeArea findByAreaName(String areaName) {
    return buildQuery(qSurgeArea)
      .where(
        qSurgeArea.name.eq(areaName),
        qSurgeArea.active.isTrue()
      )
      .fetchOne();
  }

  public List<SurgeArea> findByAreaGeometries(List<AreaGeometry> areaGeometries) {
    JPAQuery<SurgeArea> query = buildQuery(qSurgeArea);

    BooleanBuilder builder = new BooleanBuilder();
    builder.and(qSurgeArea.areaGeometry.in(areaGeometries));
    query.where(builder);

    return query.fetch();
  }

  public SurgeArea findByAreaGeometry(Long geometryId) {
    return buildQuery(qSurgeArea)
      .where(qSurgeArea.areaGeometry.id.eq(geometryId))
      .fetchOne();
  }

  public List<Ride> findSurgeAreaRides(Long cityId, Date startDate, Set<RideStatus> searchedStatuses) {
    JPAQuery<Ride> query = buildQuery(qRide);
    BooleanBuilder builder = new BooleanBuilder();
    appendDatePeriodCriteria(startDate, builder);
    appendStatusesCriteria(searchedStatuses, builder);
    builder
      .and(qRide.cityId.eq(cityId))
      .and(qRide.startAreaId.isNotNull());

    query.where(builder);
    return query.fetch();
  }

  public List<SurgeArea> findAllAutomated(Set<Long> ids) {
    return buildQuery(qSurgeArea)
      .where(
        qSurgeArea.id.in(ids),
        qSurgeArea.automated.isTrue()
      )
      .fetch();
  }

  private void appendStatusesCriteria(Set<RideStatus> searchedStatuses, BooleanBuilder builder) {
    if (CollectionUtils.isNotEmpty(searchedStatuses)) {
      builder.and(qRide.status.in(searchedStatuses));
    }
  }

  private void appendDatePeriodCriteria(Date startDate, BooleanBuilder builder) {
    if (startDate != null) {
      builder.and(qRide.updatedDate.goe(startDate));
    }
  }
}
