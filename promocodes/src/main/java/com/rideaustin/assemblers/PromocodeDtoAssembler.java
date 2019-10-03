package com.rideaustin.assemblers;

import javax.inject.Inject;

import org.springframework.stereotype.Component;

import com.rideaustin.model.promocodes.Promocode;
import com.rideaustin.rest.model.PromocodeDto;
import com.rideaustin.service.CityCache;
import com.rideaustin.service.user.CarTypesUtils;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class PromocodeDtoAssembler implements BilateralAssembler<Promocode, PromocodeDto> {

  private final CityCache cityCache;

  @Override
  public PromocodeDto toDto(Promocode promocode) {
    if (promocode == null) {
      return null;
    }
    return PromocodeDto.builder()
      .id(promocode.getId())
      .promocodeType(promocode.getPromocodeType())
      .codeLiteral(promocode.getCodeLiteral())
      .codeValue(promocode.getCodeValue())
      .title(promocode.getTitle())
      .startsOn(promocode.getStartsOn())
      .endsOn(promocode.getEndsOn())
      .newRidersOnly(promocode.isNewRidersOnly())
      .maximumRedemption(promocode.getMaximumRedemption())
      .maximumUsesPerAccount(promocode.getMaximumUsesPerAccount())
      .currentRedemption(promocode.getCurrentRedemption())
      .driverId(promocode.getDriverId())
      .carTypes(CarTypesUtils.fromBitMask(promocode.getCarTypeBitmask()))
      .cities(cityCache.fromBitMask(promocode.getCityBitmask()))
      .nextTripOnly(promocode.isNextTripOnly())
      .useEndDate(promocode.getUseEndDate())
      .applicableToFees(promocode.isApplicableToFees())
      .validForNumberOfDays(promocode.getValidForNumberOfDays())
      .validForNumberOfRides(promocode.getValidForNumberOfRides())
      .maxPromotionValue(promocode.getMaximumPromotionValue())
      .cappedAmountPerUse(promocode.getCappedAmountPerUse())
      .build();
  }

  @Override
  public Promocode toDs(PromocodeDto promocodeDto) {
    Promocode promocode = Promocode.builder()
      .promocodeType(promocodeDto.getPromocodeType())
      .codeLiteral(promocodeDto.getCodeLiteral())
      .codeValue(promocodeDto.getCodeValue())
      .startsOn(promocodeDto.getStartsOn())
      .endsOn(promocodeDto.getEndsOn())
      .title(promocodeDto.getTitle())
      .newRidersOnly(promocodeDto.isNewRidersOnly())
      .applicableToFees(promocodeDto.isApplicableToFees())
      .maximumUsesPerAccount(promocodeDto.getMaximumUsesPerAccount())
      .maximumRedemption(promocodeDto.getMaximumRedemption())
      .driverId(promocodeDto.getDriverId())
      .carTypeBitmask(CarTypesUtils.toBitMask(promocodeDto.getCarTypes()))
      .cityBitmask(cityCache.toBitMask(promocodeDto.getCities()))
      .validForNumberOfRides(promocodeDto.getValidForNumberOfRides())
      .validForNumberOfDays(promocodeDto.getValidForNumberOfDays())
      .nextTripOnly(promocodeDto.isNextTripOnly())
      .useEndDate(promocodeDto.getUseEndDate())
      .cappedAmountPerUse(promocodeDto.getCappedAmountPerUse())
      .build();

    if (promocodeDto.getId() != null) {
      promocode.setId(promocodeDto.getId());
    }

    return promocode;
  }
}