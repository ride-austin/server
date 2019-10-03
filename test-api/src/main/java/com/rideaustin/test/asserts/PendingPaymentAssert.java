package com.rideaustin.test.asserts;

import org.assertj.core.api.AbstractAssert;
import org.joda.money.Money;

import com.rideaustin.service.model.PendingPaymentDto;

public class PendingPaymentAssert extends AbstractAssert<PendingPaymentAssert, PendingPaymentDto> {
  private PendingPaymentAssert(PendingPaymentDto pendingPaymentDto) {
    super(pendingPaymentDto, PendingPaymentAssert.class);
  }

  public static PendingPaymentAssert assertThat(PendingPaymentDto pendingPaymentDto) {
    return new PendingPaymentAssert(pendingPaymentDto);
  }

  public PendingPaymentAssert hasRide(Long ride) {
    isNotNull();
    if (actual.getRideId() != ride) {
      failWithMessage("Expected to have ride %s but was %s", ride, actual.getRideId());
    }
    return this;
  }

  public PendingPaymentAssert hasAmount(Money amount) {
    isNotNull();
    if (!actual.getAmount().isEqual(amount)) {
      failWithMessage("Expected to have unpaid amount %s but was %s", amount, actual.getAmount());
    }
    return this;
  }
}
