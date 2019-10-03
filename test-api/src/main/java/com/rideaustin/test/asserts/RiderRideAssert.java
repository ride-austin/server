package com.rideaustin.test.asserts;

import java.math.BigDecimal;
import java.util.Objects;

import org.assertj.core.api.AbstractAssert;
import org.joda.money.CurrencyUnit;
import org.joda.money.Money;

import com.google.maps.model.LatLng;
import com.rideaustin.model.enums.RideStatus;
import com.rideaustin.model.enums.RideUpgradeRequestStatus;
import com.rideaustin.rest.model.MobileRiderRideDto;
import com.rideaustin.test.util.TestUtils;
import com.rideaustin.utils.SafeZeroUtils;

public class RiderRideAssert extends AbstractAssert<RiderRideAssert, MobileRiderRideDto> {

  private RiderRideAssert(MobileRiderRideDto ride) {
    super(ride, RiderRideAssert.class);
  }

  public static RiderRideAssert assertThat(MobileRiderRideDto ride) {
    return new RiderRideAssert(ride);
  }

  public RiderRideAssert hasRequestedCategory(String carCategory) {
    isNotNull();
    String actualCarCategory = actual.getRequestedCarType().getCarCategory();
    if (!Objects.equals(actualCarCategory, carCategory)) {
      failWithMessage("Expected requested car category <%s> but was <%s>", carCategory, actualCarCategory);
    }
    return this;
  }

  public RiderRideAssert hasUpgradeRequestStatus(RideUpgradeRequestStatus status) {
    isNotNull();
    if (!Objects.equals(status, actual.getUpgradeRequest().getStatus())) {
      failWithMessage("Expected upgrade request status <%s> but was <%s>", status, actual.getUpgradeRequest().getStatus());
    }
    return this;
  }

  public RiderRideAssert hasStatus(RideStatus status) {
    isNotNull();
    if (!Objects.equals(status, actual.getStatus())) {
      failWithMessage("Expected ride status <%s> but was <%s>", status, actual.getStatus());
    }
    return this;
  }

  public RiderRideAssert hasDriverAssigned(Long activeDriverId) {
    isNotNull();
    if (activeDriverId == null && actual.getActiveDriver() != null) {
      failWithMessage("Expected to have no driver assigned, but was <%s>", actual.getActiveDriver().getId());
    }
    else if (activeDriverId != null && !Objects.equals(actual.getActiveDriver().getId(), activeDriverId)) {
      failWithMessage("Wrong active driver assigned. Should be <%s> but was <%s>", activeDriverId, actual.getActiveDriver().getId());
    }
    return this;
  }

  public RiderRideAssert hasStartLocation(LatLng location) {
    return hasLocation(location, new LatLng(actual.getStartLocationLat(), actual.getStartLocationLong()), "start");
  }

  public RiderRideAssert hasEndLocation(LatLng location) {
    return hasLocation(location,
      actual.getEndLocationLat() == null || actual.getEndLocationLong() == null
        ? null
        : new LatLng(actual.getEndLocationLat(), actual.getEndLocationLong()),
      "end");
  }

  public RiderRideAssert hasETA(Long eta) {
    isNotNull();
    if (!Objects.equals(eta, actual.getEstimatedTimeArrive())) {
      failWithMessage("Expected ETA %s but was %s", eta, actual.getEstimatedTimeArrive());
    }
    return this;
  }

  private RiderRideAssert hasLocation(LatLng location, LatLng actualLocation, String label) {
    isNotNull();
    if (!TestUtils.locationEquals(actualLocation, location)) {
      failWithMessage("Expected "+ label +" location %s but was %s", location, actualLocation);
    }
    return this;
  }

  public RiderRideAssert hasDriverPayment(BigDecimal expected) {
    isNotNull();
    BigDecimal actualAmount = SafeZeroUtils.safeZero(actual.getDriverPayment()).getAmount();
    if (expected.compareTo(actualAmount) != 0) {
      failWithMessage("Expected driver payment %.2f but was %.2f", expected, actualAmount);
    }
    return this;
  }

  public RiderRideAssert hasNoRoundUp() {
    isNotNull();
    if (!actual.getRoundUpAmount().isZero()) {
      failWithMessage("Expected round up amount to be zero but was <%s>", actual.getRoundUpAmount());
    }
    return this;
  }

  public RiderRideAssert hasRoundUpAmount(double expected) {
    isNotNull();
    if (Money.of(CurrencyUnit.USD, expected).compareTo(actual.getRoundUpAmount()) != 0) {
      failWithMessage("Expected round-up %s but was %s", expected, actual.getRoundUpAmount());
    }
    return this;
  }

  public RiderRideAssert hasTip(double expected) {
    isNotNull();
    if (Money.of(CurrencyUnit.USD, expected).compareTo(actual.getTip()) != 0) {
      failWithMessage("Expected tip %s but was %s", expected, actual.getTip());
    }
    return this;
  }

  public RiderRideAssert hasFreeCreditCharged(BigDecimal expected) {
    isNotNull();
    if (Money.of(CurrencyUnit.USD, expected).compareTo(actual.getFreeCreditCharged()) != 0) {
      failWithMessage("Expected free credit charged %s but was %s", expected, actual.getFreeCreditCharged());
    }
    return this;
  }

  public RiderRideAssert hasTotalCharge(double expected) {
    isNotNull();
    if (Money.of(CurrencyUnit.USD, expected).compareTo(actual.getTotalCharge()) != 0) {
      failWithMessage("Expected total charge %s but was %s", expected, actual.getTotalCharge());
    }
    return this;
  }
}
