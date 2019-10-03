package com.rideaustin.service.user;

import com.google.common.collect.ImmutableMap;
import com.rideaustin.model.MessagingRideInfoDTO;
import com.rideaustin.service.thirdparty.AbstractTemplateSMS;

public class DriverReachedSMS extends AbstractTemplateSMS {

  private static final String TEMPLATE = "driver_reached_sms.ftl";

  public DriverReachedSMS(MessagingRideInfoDTO info) {
    super(TEMPLATE);
    setModel(ImmutableMap.of(
      "driver", info.getDriverFirstName(),
      "license", info.getLicense(),
      "color", info.getColor(),
      "make", info.getMake(),
      "model", info.getModel()
    ));
    addRecipient(info.getRiderPhoneNumber());
  }
}
