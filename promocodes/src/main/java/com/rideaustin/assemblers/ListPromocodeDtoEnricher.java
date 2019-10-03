package com.rideaustin.assemblers;

import java.math.BigDecimal;

import javax.inject.Inject;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import com.rideaustin.rest.model.ListPromocodeDto;
import com.rideaustin.service.CityCache;
import com.rideaustin.service.user.CarTypesCache;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class ListPromocodeDtoEnricher implements DTOEnricher<ListPromocodeDto>, Converter<ListPromocodeDto, ListPromocodeDto> {

  private final CarTypesCache carTypesCache;
  private final CityCache cityCache;

  @Override
  public ListPromocodeDto enrich(ListPromocodeDto source) {
    if (source == null) {
      return null;
    }
    source.setCarTypes(carTypesCache.fromBitMask(source.getCarTypesBitmask()));
    source.setCities(cityCache.fromBitMask(source.getCityBitmask()));
    source.setMaxPromotionValue(getMaximumPromotionValue(source));
    return source;
  }

  private BigDecimal getMaximumPromotionValue(ListPromocodeDto source) {
    if (source.getMaximumRedemption() != null && source.getCodeValue() != null) {
      return source.getCodeValue().multiply(BigDecimal.valueOf(source.getMaximumRedemption()));
    }
    return null;
  }

  @Override
  public ListPromocodeDto convert(ListPromocodeDto source) {
    return enrich(source);
  }
}
