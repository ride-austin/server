package com.rideaustin.service.user;

import com.google.common.collect.ImmutableMap;
import com.rideaustin.model.PhoneVerificationItem;
import com.rideaustin.service.thirdparty.AbstractTemplateSMS;

public class PhoneVerificationSMS extends AbstractTemplateSMS {

  private static final String TEMPLATE = "phone_verification_sms.ftl";

  public PhoneVerificationSMS(PhoneVerificationItem verificationItem) {
    super(TEMPLATE);
    setModel(ImmutableMap.of("verificationCode", verificationItem.getVerificationCode()));
    addRecipient(verificationItem.getPhoneNumber());
  }
}
