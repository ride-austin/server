package com.rideaustin.assemblers;

import java.util.Date;

import javax.inject.Inject;

import org.springframework.stereotype.Component;

import com.rideaustin.Constants;
import com.rideaustin.model.splitfare.FarePayment;
import com.rideaustin.rest.model.FarePaymentDto;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class FarePaymentDtoAssembler implements SingleSideAssembler<FarePayment, FarePaymentDto> {

  private final RiderCardDtoAssembler cardDtoAssembler;

  @Override
  public FarePaymentDto toDto(FarePayment source) {
    if (source == null) {
      return null;
    }
    FarePaymentDto.FarePaymentDtoBuilder builder = FarePaymentDto.builder()
      .id(source.getId())
      .rideId(source.getRide().getId())
      .riderId(source.getRider().getId())
      .riderFullName(source.getRider().getFullName())
      .status(source.getSplitStatus())
      .createdDate(formatDate(source.getCreatedDate()))
      .chargeId(source.getChargeId())
      .riderPhoto(source.getRider().getUser().getPhotoUrl())
      .paymentProvider(source.getProvider())
      .paymentStatus(source.getPaymentStatus())
      .mainRider(source.isMainRider());
    if (source.getUsedCard() != null) {
      builder.usedCard(cardDtoAssembler.toDto(source.getUsedCard()));
    }
    if (source.getUpdatedDate() != null) {
      builder.updatedDate(formatDate(source.getUpdatedDate()));
    }
    if (source.getFreeCreditCharged() != null) {
      builder.freeCreditCharged(source.getFreeCreditCharged());
    }
    if (source.getStripeCreditCharge() != null) {
      builder.stripeCreditCharge(source.getStripeCreditCharge());
    }

    return builder.build();
  }

  private String formatDate(Date date) {
    return Constants.DATE_FORMATTER.format(date.toInstant().atZone(Constants.CST_ZONE));
  }
}
