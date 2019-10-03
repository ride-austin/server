package com.rideaustin;

import org.assertj.core.api.AbstractAssert;

import com.rideaustin.service.model.context.DispatchContext;

public class DispatchContextAssert extends AbstractAssert<DispatchContextAssert, DispatchContext> {
  private DispatchContextAssert(DispatchContext dispatchContext) {
    super(dispatchContext, DispatchContextAssert.class);
  }

  public static DispatchContextAssert assertThat(DispatchContext context) {
    return new DispatchContextAssert(context);
  }

  public DispatchContextAssert isAccepted() {
    isNotNull();
    if (!actual.isAccepted()) {
      failWithMessage("Expected to be accepted");
    }
    return this;
  }
}
