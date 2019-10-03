package com.rideaustin.repo.dsl;

import org.springframework.stereotype.Repository;

import com.rideaustin.model.PhoneVerificationItem;
import com.rideaustin.model.QPhoneVerificationItem;

@Repository
public class PhoneVerificationItemDslRepository extends AbstractDslRepository {

  private static QPhoneVerificationItem qPhoneVerificationItem = QPhoneVerificationItem.phoneVerificationItem;

  public PhoneVerificationItem findVerificationItem(String authToken, String code) {
    return buildQuery(qPhoneVerificationItem)
      .where(qPhoneVerificationItem.authToken.eq(authToken)
        .and(qPhoneVerificationItem.verificationCode.eq(code)))
      .fetchOne();
  }
}
