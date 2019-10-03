package com.rideaustin.service.model;

import static com.rideaustin.utils.SafeZeroUtils.safeZero;

import org.joda.money.Money;

import com.rideaustin.model.enums.PaymentProvider;
import com.rideaustin.model.enums.PaymentStatus;
import com.rideaustin.rest.model.RiderCardDto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FarePaymentDto {

  private final RiderCardDto usedCard;
  private final boolean mainRider;
  private final Money freeCreditCharged;
  private final Money stripeCreditCharge;
  private final String riderFullName;
  private final PaymentProvider paymentProvider;
  private final PaymentStatus paymentStatus;

  public Money getSumOfCharged() {
    return safeZero(freeCreditCharged).plus(safeZero(stripeCreditCharge));
  }

}
