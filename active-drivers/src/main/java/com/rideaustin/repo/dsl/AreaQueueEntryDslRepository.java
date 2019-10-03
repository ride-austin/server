package com.rideaustin.repo.dsl;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Repository;

import com.querydsl.core.BooleanBuilder;
import com.rideaustin.model.Area;
import com.rideaustin.model.AreaQueueEntry;
import com.rideaustin.model.QAreaQueueEntry;
import com.rideaustin.model.enums.ActiveDriverStatus;
import com.rideaustin.model.ride.CarType;

@Repository
public class AreaQueueEntryDslRepository extends AbstractDslRepository {

  private static final QAreaQueueEntry qAreaQueueEntry = QAreaQueueEntry.areaQueueEntry;

  public List<AreaQueueEntry> findByArea(Area area) {
    return buildQuery(qAreaQueueEntry)
      .where(qAreaQueueEntry.area.eq(area)
        .and(qAreaQueueEntry.enabled.isTrue()))
      .orderBy(qAreaQueueEntry.id.asc())
      .fetch();
  }

  public List<AreaQueueEntry> findEnabledByActiveDriver(Long activeDriverId) {
    return buildQuery(qAreaQueueEntry)
      .where(
        qAreaQueueEntry.activeDriver.id.eq(activeDriverId),
        qAreaQueueEntry.enabled.isTrue()
      )
      .orderBy(qAreaQueueEntry.id.asc())
      .fetch();
  }

  public List<AreaQueueEntry> findEnabledForOfflineActiveDriver(Long driverId) {
    return buildQuery(qAreaQueueEntry)
      .where(
        qAreaQueueEntry.activeDriver.driver.id.eq(driverId),
        qAreaQueueEntry.activeDriver.status.eq(ActiveDriverStatus.INACTIVE),
        qAreaQueueEntry.enabled.isTrue()
      )
      .orderBy(qAreaQueueEntry.id.asc())
      .fetch();
  }

  public List<Long> findActiveDriverIdsByCarCategory(Area area, CarType carType) {
    return buildQuery(qAreaQueueEntry)
      .select(qAreaQueueEntry.activeDriver.id)
      .where(
        qAreaQueueEntry.area.eq(area),
        qAreaQueueEntry.carCategory.eq(carType.getCarCategory()),
        qAreaQueueEntry.enabled.isTrue()
      )
      .orderBy(qAreaQueueEntry.id.asc())
      .fetch();
  }

  public List<Long> findQueuedAvailableActiveDriverIds(Area area, List<Long> ignoreIds, String carCategory) {
    BooleanBuilder where = new BooleanBuilder()
      .and(qAreaQueueEntry.area.eq(area))
      .and(qAreaQueueEntry.enabled.isTrue())
      .and(qAreaQueueEntry.activeDriver.status.eq(ActiveDriverStatus.AVAILABLE))
      .and(qAreaQueueEntry.carCategory.eq(carCategory));

    if (!CollectionUtils.isEmpty(ignoreIds)) {
      where.and(qAreaQueueEntry.activeDriver.id.notIn(ignoreIds));
    }

    return buildQuery(qAreaQueueEntry)
      .select(qAreaQueueEntry.activeDriver.id)
      .where(where)
      .fetch();
  }

  public List<AreaQueueEntry> findEnabledByActiveDriver(long id, Area area) {
    return buildQuery(qAreaQueueEntry)
      .where(
        qAreaQueueEntry.activeDriver.id.eq(id),
        qAreaQueueEntry.area.eq(area),
        qAreaQueueEntry.enabled.isTrue()
        )
      .fetch();
  }
}
