package com.rideaustin.test.asserts;

import java.util.Objects;

import org.assertj.core.api.AbstractAssert;

import com.rideaustin.model.enums.DriverActivationStatus;
import com.rideaustin.model.user.Driver;

public class DriverAssert extends AbstractAssert<DriverAssert, Driver> {
  private DriverAssert(Driver driver) {
    super(driver, DriverAssert.class);
  }

  public static DriverAssert assertThat(Driver driver) {
    return new DriverAssert(driver);
  }

  public DriverAssert isNotActive() {
    isNotNull();
    if (actual.isActive()) {
      failWithMessage("Driver is expected to be not active but was active");
    }
    return this;
  }

  public DriverAssert hasStatus(DriverActivationStatus expected) {
    isNotNull();
    if (!Objects.equals(actual.getActivationStatus(), expected)) {
      failWithMessage("Driver is expected to have status <%s> but was <%s>", expected, actual.getActivationStatus());
    }
    return this;
  }
}
