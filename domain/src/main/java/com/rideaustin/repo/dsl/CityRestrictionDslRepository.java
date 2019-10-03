package com.rideaustin.repo.dsl;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.rideaustin.model.CityRestriction;
import com.rideaustin.model.QCityRestriction;

@Repository
public class CityRestrictionDslRepository extends AbstractDslRepository {

  private static final QCityRestriction qCityRestriction = QCityRestriction.cityRestriction;

  public List<CityRestriction> findByCity(long cityId) {
    return buildQuery(qCityRestriction)
      .where(
        qCityRestriction.cityId.eq(cityId),
        qCityRestriction.enabled.isTrue()
      )
      .fetch();
  }
}
