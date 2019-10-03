package com.rideaustin.service.thirdparty.lookup.condition;

import static com.rideaustin.Constants.Configuration.LOOKUP_SERVICE_MOCK;

public class MockLookupServiceCondition extends LookupServiceCondition {
  @Override
  protected String getValue() {
    return LOOKUP_SERVICE_MOCK;
  }
}
