package com.rideaustin.test.setup;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Primary
@Component
public class DefaultApplePaySetup extends BaseApplePaySetup<DefaultApplePaySetup> {
  @Override
  protected DefaultApplePaySetup getThis() {
    return this;
  }
}
