package com.rideaustin.repo.dsl;

import java.util.Date;
import java.util.List;

import org.springframework.stereotype.Repository;

import com.rideaustin.model.CustomPayment;
import com.rideaustin.model.QCustomPayment;

@Repository
public class CustomPaymentReportDslRepository extends AbstractDslRepository {

  private static final QCustomPayment qCustomPayment = QCustomPayment.customPayment;

  public List<CustomPayment> findBetweenDates(Date beginDate, Date endDate, Long cityId) {
    return buildQuery(qCustomPayment)
      .where(qCustomPayment.createdDate.between(beginDate, endDate))
      .where(qCustomPayment.driver.cityId.eq(cityId))
      .fetch();
  }
}
