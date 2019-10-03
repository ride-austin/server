package com.rideaustin.repo.dsl;

import java.util.List;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;

import com.rideaustin.model.Area;
import com.rideaustin.model.QArea;
import com.rideaustin.service.areaqueue.AreaCache;

@Repository
public class AreaDslRepository extends AbstractDslRepository {

  private static final QArea qArea = QArea.area;

  @Cacheable(AreaCache.AREAS_CACHE)
  public List<Area> findAll() {
    return buildQuery(qArea)
      .where(qArea.enabled.isTrue())
      .orderBy(qArea.id.asc())
      .fetch();
  }

  public Area findOne(Long id) {
    return buildQuery(qArea)
      .where(qArea.id.eq(id))
      .fetchOne();
  }

  public List<Area> findByKeys(String... keys) {
    return buildQuery(qArea)
      .where(qArea.key.in(keys))
      .fetch();
  }

}
