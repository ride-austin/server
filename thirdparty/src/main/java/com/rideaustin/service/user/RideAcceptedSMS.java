package com.rideaustin.service.user;

import java.math.BigDecimal;

import com.google.common.collect.ImmutableMap;
import com.rideaustin.Constants;
import com.rideaustin.model.MessagingRideInfoDTO;
import com.rideaustin.service.thirdparty.AbstractTemplateSMS;
import com.rideaustin.utils.SafeZeroUtils;

public class RideAcceptedSMS extends AbstractTemplateSMS {

  private static final String TEMPLATE = "ride_accepted_sms.ftl";

  public RideAcceptedSMS(MessagingRideInfoDTO info) {
    super(TEMPLATE);
    setModel(ImmutableMap.<String, Object>builder()
      .put("driver", info.getDriverFirstName())
      .put("license", info.getLicense())
      .put("color", info.getColor())
      .put("make", info.getMake())
      .put("model", info.getModel())
      .put("eta", SafeZeroUtils.safeZero(BigDecimal.valueOf(info.getDrivingTimeToRider())).divide(BigDecimal.valueOf(60), 0, Constants.ROUNDING_MODE).longValue())
      .build()
    );
    addRecipient(info.getRiderPhoneNumber());
  }
}
