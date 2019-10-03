package com.rideaustin.test.setup;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Primary
@Component
public class DefaultWODispatchSetup extends BaseWODispatchSetup<DefaultWODispatchSetup> {
  @Override
  protected DefaultWODispatchSetup getThis() {
    return this;
  }
}
