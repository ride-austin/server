package com.rideaustin.repo.dsl;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.rideaustin.model.ride.DriverType;
import com.rideaustin.model.ride.QDriverType;

@Repository
public class DriverTypeDslRepository extends AbstractDslRepository {

  private static final QDriverType qDriverType = QDriverType.driverType;

  public List<DriverType> getAllEnabledOrdered() {
    return buildQuery(qDriverType)
      .where(qDriverType.enabled.isTrue())
      .orderBy(qDriverType.name.desc())
      .fetch();
  }

}
