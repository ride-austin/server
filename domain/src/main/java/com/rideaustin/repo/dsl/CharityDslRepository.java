package com.rideaustin.repo.dsl;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.rideaustin.model.Charity;
import com.rideaustin.model.City;
import com.rideaustin.model.QCharity;

@Repository
public class CharityDslRepository extends AbstractDslRepository {

  private static final QCharity qCharity = QCharity.charity;

  public List<Charity> findAllByCity(City city) {
    int bitmask = city.getBitmask();
    return buildQuery(qCharity)
      .where(
        bitmaskPredicate(qCharity.cityBitmask, bitmask),
        qCharity.enabled.isTrue()
      )
      .orderBy(qCharity.order.asc()).orderBy(qCharity.name.asc())
      .fetch();
  }
}
