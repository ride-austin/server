package com.rideaustin.test.asserts;

import org.assertj.core.api.AbstractAssert;

import com.rideaustin.model.enums.SplitFareStatus;
import com.rideaustin.rest.model.FarePaymentDto;

public class FarePaymentAssert extends AbstractAssert<FarePaymentAssert, FarePaymentDto> {

  private FarePaymentAssert(FarePaymentDto farePaymentDto) {
    super(farePaymentDto, FarePaymentAssert.class);
  }

  public static FarePaymentAssert assertThat(FarePaymentDto farePaymentDto) {
    return new FarePaymentAssert(farePaymentDto);
  }

  public FarePaymentAssert isAccepted() {
    isNotNull();
    if (actual.getStatus() != SplitFareStatus.ACCEPTED) {
      failWithMessage("Fare payment is expected to be accepted but was %s", actual.getStatus());
    }
    return this;
  }

  public FarePaymentAssert isCharged() {
    isNotNull();
    if (actual.getChargeId() == null) {
      failWithMessage("Fare payment is expected to be charged");
    }
    return this;
  }

  public FarePaymentAssert hasCharge(double value) {
    isNotNull();
    isCharged();
    if (Double.compare(actual.getStripeCreditCharge().getAmount().doubleValue(), value) != 0) {
      failWithMessage("Fare payment is expected to be charged for %.2f but was charged for %.2f", value, actual.getStripeCreditCharge().getAmount().doubleValue());
    }
    return this;
  }
}
