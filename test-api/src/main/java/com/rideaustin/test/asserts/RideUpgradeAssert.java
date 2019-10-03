package com.rideaustin.test.asserts;

import java.math.BigDecimal;
import java.util.Objects;

import org.assertj.core.api.AbstractAssert;

import com.rideaustin.model.enums.RideUpgradeRequestStatus;
import com.rideaustin.model.ride.RideUpgradeRequest;

public class RideUpgradeAssert extends AbstractAssert<RideUpgradeAssert, RideUpgradeRequest> {

  private RideUpgradeAssert(RideUpgradeRequest request) {
    super(request, RideUpgradeAssert.class);
  }

  public static RideUpgradeAssert assertThat(RideUpgradeRequest request) {
    return new RideUpgradeAssert(request);
  }

  public RideUpgradeAssert hasSource(String source) {
    isNotNull();
    if (!Objects.equals(actual.getSource(), source)) {
      failWithMessage("Expected source %s but was %s", source, actual.getSource());
    }
    return this;
  }

  public RideUpgradeAssert hasTarget(String target) {
    isNotNull();
    if (!Objects.equals(actual.getTarget(), target)) {
      failWithMessage("Expected target %s but was %s", target, actual.getTarget());
    }
    return this;
  }

  public RideUpgradeAssert hasStatus(RideUpgradeRequestStatus status) {
    isNotNull();
    if (!Objects.equals(actual.getStatus(), status)) {
      failWithMessage("Expected status %s but was %s", status, actual.getStatus());
    }
    return this;
  }

  public RideUpgradeAssert hasSurgeFactor(BigDecimal surgeFactor) {
    isNotNull();
    if (actual.getSurgeFactor().compareTo(surgeFactor) != 0) {
      failWithMessage("Expected surge factor %s but was %s", surgeFactor, actual.getSurgeFactor());
    }
    return this;
  }

  public RideUpgradeAssert belongsToRide(long rideId) {
    isNotNull();
    if (!Objects.equals(actual.getRideId(), rideId)) {
      failWithMessage("Expected ride %d but was %d", rideId, actual.getRideId());
    }
    return this;
  }

  public RideUpgradeAssert isRequestedBy(long driverId) {
    isNotNull();
    if (!Objects.equals(actual.getRequestedBy(), driverId)) {
      failWithMessage("Expected requester %d but was %d", driverId, actual.getRequestedBy());
    }
    return this;
  }

  public RideUpgradeAssert isRequestedFrom(long riderId) {
    isNotNull();
    if (!Objects.equals(actual.getRequestedFrom(), riderId)) {
      failWithMessage("Expected requestee %d but was %d", riderId, actual.getRequestedFrom());
    }
    return this;
  }

}
