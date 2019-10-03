package com.rideaustin.assemblers;

import org.springframework.stereotype.Component;

import com.rideaustin.model.user.RiderCard;
import com.rideaustin.rest.model.RiderCardDto;

@Component
public class RiderCardDtoAssembler implements SingleSideAssembler<RiderCard, RiderCardDto> {

  @Override
  public RiderCardDto toDto(RiderCard riderCard) {
    if (riderCard == null) {
      return null;
    }
    return RiderCardDto.builder()
      .id(riderCard.getId())
      .cardBrand(riderCard.getCardBrand())
      .cardExpired(riderCard.isCardExpired())
      .expirationMonth(riderCard.getExpirationMonth())
      .expirationYear(riderCard.getExpirationYear())
      .cardNumber(riderCard.getCardNumber())
      .primary(riderCard.isPrimary())
      .build();
  }
  
}