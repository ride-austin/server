package com.rideaustin.assemblers;

import java.text.SimpleDateFormat;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import com.rideaustin.model.CustomPayment;
import com.rideaustin.rest.model.CustomPaymentDto;

@Component
public class CustomPaymentDtoAssembler implements SingleSideAssembler<CustomPayment, CustomPaymentDto>, Converter<CustomPayment, CustomPaymentDto> {

  private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

  @Override
  public CustomPaymentDto toDto(CustomPayment source) {
    if (source == null) {
      return null;
    }
    CustomPaymentDto customPaymentDto = new CustomPaymentDto(source.getId(), source.getDriver().getId(),
      source.getDriver().getFirstname(), source.getDriver().getLastname(), source.getDriver().getEmail(), source.getCreator().getId(),
      source.getCreator().getFirstname(), source.getCreator().getLastname(), source.getCreator().getEmail(), source.getValue(),
      source.getCategory(), source.getDescription());

    if (source.getPaymentDate() != null) {
      customPaymentDto.setPaymentDate(sdf.format(source.getPaymentDate()));
    }
    return customPaymentDto;
  }

  @Override
  public CustomPaymentDto convert(CustomPayment source) {
    return toDto(source);
  }
}
