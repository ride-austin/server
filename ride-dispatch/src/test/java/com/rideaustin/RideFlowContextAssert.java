package com.rideaustin;

import java.util.Date;
import java.util.Objects;

import org.assertj.core.api.AbstractAssert;

import com.rideaustin.service.model.context.RideFlowContext;

public class RideFlowContextAssert extends AbstractAssert<RideFlowContextAssert, RideFlowContext> {
  private RideFlowContextAssert(RideFlowContext rideFlowContext) {
    super(rideFlowContext, RideFlowContextAssert.class);
  }

  public static RideFlowContextAssert assertThat(RideFlowContext context) {
    return new RideFlowContextAssert(context);
  }

  public RideFlowContextAssert hasDriver(long driver) {
    isNotNull();
    if (actual.getDriver() != driver) {
      failWithMessage("Expected to have driver assigned <%d> but was <%d>", driver, actual.getDriver());
    }
    return this;
  }

  public RideFlowContextAssert hasNoSession() {
    isNotNull();
    if (actual.getDriverSession() != null) {
      failWithMessage("Expected to have no driver session set");
    }
    return this;
  }

  public RideFlowContextAssert hasAcceptedOn(Date expected) {
    isNotNull();
    Date actualSeconds = truncateToSeconds(actual.getAcceptedOn());
    Date expectedSeconds = truncateToSeconds(expected);
    if (!Objects.equals(actualSeconds, expectedSeconds)) {
      failWithMessage("Expected to have acceptedOn timestamp <%d> but was <%d>", expectedSeconds.getTime(), actualSeconds.getTime());
    }
    return this;
  }

  public RideFlowContextAssert hasSession(long expected) {
    isNotNull();
    if (actual.getDriverSession() != expected) {
      failWithMessage("Expected to have driver session <%d> but was <%d>", expected, actual.getDriverSession());
    }
    return this;
  }

  private static Date truncateToSeconds(Date date) {
    return new Date((date.getTime() / 1000)*1000);
  }
}
