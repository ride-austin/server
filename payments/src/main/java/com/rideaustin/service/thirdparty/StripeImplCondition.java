package com.rideaustin.service.thirdparty;

public class StripeImplCondition extends StripeCondition {

  @Override
  protected String getValue() {
    return "stripe";
  }

}