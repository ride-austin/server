package com.rideaustin.service.promocodes;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.function.Function;

import javax.inject.Inject;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import com.rideaustin.rest.model.PromocodeDto;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class PromocodeDtoValidator implements Validator {

  private static final int MAX_LITERAL_LENGTH = 30;
  private final PromocodeServiceConfig promocodeServiceConfig;

  @Override
  public boolean supports(Class<?> clazz) {
    return PromocodeDto.class.isAssignableFrom(clazz);
  }

  @Override
  public void validate(Object target, Errors errors) {
    PromocodeDto dto = (PromocodeDto) target;
    if (CollectionUtils.isEmpty(dto.getCities())) {
      errors.rejectValue("", "", "Please select city");
    }
    if (CollectionUtils.isEmpty(dto.getCarTypes())) {
      errors.rejectValue("", "", "Please select car category");
    }
    if (StringUtils.isEmpty(dto.getCodeLiteral())) {
      errors.rejectValue("", "", "Promocode cannot be empty");
    }
    if (safeCondition(dto.getCodeLiteral(), l -> l.length() > MAX_LITERAL_LENGTH)) {
      errors.rejectValue("", "", "Promocode too long");
    }
    if (dto.getCodeValue() == null) {
      errors.rejectValue("", "", "Promocode value cannot be empty");
    }
    if (safeCondition(dto.getCodeValue(), v -> v.compareTo(BigDecimal.ZERO) < 1)) {
      errors.rejectValue("", "", "Value cannot be less than zero");
    }
    if (safeCondition(dto.getCodeValue(), v -> v.compareTo(promocodeServiceConfig.getMaximumPromocodeValue()) > 0)) {
      errors.rejectValue("", "", "Promocode value too high. Max value: " + promocodeServiceConfig.getMaximumPromocodeValue());
    }
    if (safeCondition(dto.getStartsOn(), d -> safeCondition(dto.getEndsOn(), d::after))) {
      errors.rejectValue("", "", "Start date can't be after ending date");
    }
    if (safeCondition(dto.getCappedAmountPerUse(), c -> dto.getMaximumUsesPerAccount() == null || safeCondition(dto.getMaximumUsesPerAccount(), m -> c.multiply(BigDecimal.valueOf(m.longValue())).compareTo(dto.getCodeValue()) != 0))) {
      errors.rejectValue("", "", "Capped amount per use and maximum uses per account must fit total code value");
    }
  }

  private <T> Boolean safeCondition(T field, Function<T, Boolean> condition) {
    return Optional.ofNullable(field).map(condition).orElse(false);
  }
}
