package com.rideaustin.test.setup;

import org.springframework.stereotype.Component;

@Component
public class BaseRidesInQueueSetup extends AbstractRidesInQueueSetup<BaseRidesInQueueSetup> {
  @Override
  protected BaseRidesInQueueSetup doSetup() {
    return this;
  }
}
