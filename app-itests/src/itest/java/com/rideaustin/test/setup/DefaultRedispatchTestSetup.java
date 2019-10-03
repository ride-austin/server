package com.rideaustin.test.setup;

import org.springframework.stereotype.Component;

@Component
public class DefaultRedispatchTestSetup extends BaseRedispatchTestSetup<DefaultRedispatchTestSetup> {
  @Override
  protected DefaultRedispatchTestSetup getThis() {
    return this;
  }
}
