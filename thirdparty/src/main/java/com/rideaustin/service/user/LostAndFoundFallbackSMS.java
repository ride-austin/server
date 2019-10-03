package com.rideaustin.service.user;

import com.google.common.collect.ImmutableMap;
import com.rideaustin.service.thirdparty.AbstractTemplateSMS;

public class LostAndFoundFallbackSMS extends AbstractTemplateSMS {

  private static final String TEMPLATE = "lost_and_found_fallback_sms.ftl";

  public LostAndFoundFallbackSMS(String riderPhoneNumber, String driverPhoneNumber) {
    super(TEMPLATE);
    setModel(ImmutableMap.of("riderPhoneNumber", riderPhoneNumber));
    addRecipient(driverPhoneNumber);
  }

}
