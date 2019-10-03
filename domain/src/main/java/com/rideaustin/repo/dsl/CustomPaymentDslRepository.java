package com.rideaustin.repo.dsl;

import java.util.Date;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Repository;

import com.querydsl.core.BooleanBuilder;
import com.rideaustin.model.CustomPayment;
import com.rideaustin.model.QCustomPayment;
import com.rideaustin.model.user.Driver;
import com.rideaustin.rest.model.PagingParams;

@Repository
public class CustomPaymentDslRepository extends AbstractDslRepository {

  private static final QCustomPayment qCustomPayment = QCustomPayment.customPayment;

  public List<CustomPayment> findForDriverBetweenDates(Driver driver, Date beginDate, Date endDate) {
    return buildQuery(qCustomPayment)
      .where(qCustomPayment.createdDate.between(beginDate, endDate)
        .and(qCustomPayment.driver.eq(driver)))
      .fetch();
  }

  public CustomPayment findById(Long id) {
    return buildQuery(qCustomPayment)
      .where(qCustomPayment.id.eq(id))
      .fetchOne();
  }

  public Page<CustomPayment> findOtherPayments(BooleanBuilder builder, PagingParams paging) {
    return getPage(paging, builder, qCustomPayment);
  }
}
