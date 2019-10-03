package com.rideaustin.test.asserts;

import java.math.BigDecimal;
import java.util.Objects;

import org.assertj.core.api.AbstractAssert;
import org.joda.money.CurrencyUnit;
import org.joda.money.Money;

import com.google.maps.model.LatLng;
import com.rideaustin.model.enums.RideStatus;
import com.rideaustin.model.enums.RideUpgradeRequestStatus;
import com.rideaustin.rest.model.MobileDriverRideDto;
import com.rideaustin.test.util.TestUtils;
import com.rideaustin.utils.SafeZeroUtils;

public class DriverRideAssert extends AbstractAssert<DriverRideAssert, MobileDriverRideDto> {

  private DriverRideAssert(MobileDriverRideDto ride) {
    super(ride, DriverRideAssert.class);
  }

  public static DriverRideAssert assertThat(MobileDriverRideDto ride) {
    return new DriverRideAssert(ride);
  }

  public DriverRideAssert hasRequestedCategory(String carCategory) {
    isNotNull();
    String actualCarCategory = actual.getRequestedCarType().getCarCategory();
    if (!Objects.equals(actualCarCategory, carCategory)) {
      failWithMessage("Expected requested car category <%s> but was <%s>", carCategory, actualCarCategory);
    }
    return this;
  }

  public DriverRideAssert hasUpgradeRequestStatus(RideUpgradeRequestStatus status) {
    isNotNull();
    if (!Objects.equals(status, actual.getUpgradeRequest().getStatus())) {
      failWithMessage("Expected upgrade request status <%s> but was <%s>", status, actual.getUpgradeRequest().getStatus());
    }
    return this;
  }

  public DriverRideAssert hasStatus(RideStatus status) {
    isNotNull();
    if (!Objects.equals(status, actual.getStatus())) {
      failWithMessage("Expected ride status <%s> but was <%s>", status, actual.getStatus());
    }
    return this;
  }

  public DriverRideAssert hasStartLocation(LatLng location) {
    return hasLocation(location, new LatLng(actual.getStartLocationLat(), actual.getStartLocationLong()), "start");
  }

  public DriverRideAssert hasEndLocation(LatLng location) {
    return hasLocation(location,
      actual.getEndLocationLat() == null || actual.getEndLocationLong() == null
        ? null
        : new LatLng(actual.getEndLocationLat(), actual.getEndLocationLong()),
      "end");
  }

  public DriverRideAssert hasETA(Long eta) {
    isNotNull();
    if (!Objects.equals(eta, actual.getEstimatedTimeArrive())) {
      failWithMessage("Expected ETA %s but was %s", eta, actual.getEstimatedTimeArrive());
    }
    return this;
  }

  public DriverRideAssert hasRider(long riderId) {
    isNotNull();
    if (!Objects.equals(riderId, actual.getRider().getId())) {
      failWithMessage("Expected Rider %s but was %s", riderId, actual.getRider().getId());
    }
    return this;
  }

  public DriverRideAssert hasSurgeFactor(BigDecimal surgeFactor) {
    isNotNull();
    if (surgeFactor.compareTo(actual.getSurgeFactor()) != 0) {
      failWithMessage("Expected surge factor %.2f but was %.2f", surgeFactor, actual.getSurgeFactor());
    }
    return this;
  }

  private DriverRideAssert hasLocation(LatLng location, LatLng actualLocation, String label) {
    isNotNull();
    if (!TestUtils.locationEquals(actualLocation, location)) {
      failWithMessage("Expected "+ label +" location %s but was %s", location, actualLocation);
    }
    return this;
  }

  public DriverRideAssert hasDriverPayment(BigDecimal expected) {
    isNotNull();
    BigDecimal actualAmount = SafeZeroUtils.safeZero(actual.getDriverPayment()).getAmount();
    if (expected.compareTo(actualAmount) != 0) {
      failWithMessage("Expected driver payment %.2f but was %.2f", expected, actualAmount);
    }
    return this;
  }

  public DriverRideAssert hasFreeCreditCharged(BigDecimal expected) {
    isNotNull();
    if (Money.of(CurrencyUnit.USD, expected).compareTo(actual.getFreeCreditCharged()) != 0) {
      failWithMessage("Expected free credit charged %s but was %s", expected, actual.getFreeCreditCharged());
    }
    return this;
  }

}
