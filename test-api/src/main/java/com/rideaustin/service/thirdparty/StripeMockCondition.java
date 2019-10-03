package com.rideaustin.service.thirdparty;

public class StripeMockCondition extends StripeCondition {

  @Override
  protected String getValue() {
    return "mock";
  }

}