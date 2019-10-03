package com.rideaustin.service.thirdparty.lookup.condition;

import static com.rideaustin.Constants.Configuration.LOOKUP_SERVICE_TWILIO;

public class TwilioLookupServiceCondition extends LookupServiceCondition {
  @Override
  protected String getValue() {
    return LOOKUP_SERVICE_TWILIO;
  }
}
