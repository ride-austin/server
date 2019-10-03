package com.rideaustin.assemblers;

import java.util.Optional;

import javax.inject.Inject;

import org.springframework.stereotype.Component;

import com.rideaustin.model.splitfare.FarePayment;
import com.rideaustin.service.model.FarePaymentDto;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class FarePaymentDtoEmailAssembler implements SingleSideAssembler<FarePayment, FarePaymentDto> {

  private final RiderCardDtoAssembler cardDtoAssembler;

  @Override
  public FarePaymentDto toDto(FarePayment farePayment) {
    if (farePayment == null) {
      return null;
    }
    FarePaymentDto.FarePaymentDtoBuilder builder = FarePaymentDto.builder()
      .stripeCreditCharge(farePayment.getStripeCreditCharge())
      .freeCreditCharged(farePayment.getFreeCreditCharged())
      .mainRider(farePayment.isMainRider())
      .riderFullName(farePayment.getRider().getFullName())
      .paymentProvider(farePayment.getProvider())
      .paymentStatus(farePayment.getPaymentStatus())
      .usedCard(Optional.ofNullable(farePayment.getUsedCard()).map(cardDtoAssembler::toDto).orElse(null));
    return builder.build();
  }
}
