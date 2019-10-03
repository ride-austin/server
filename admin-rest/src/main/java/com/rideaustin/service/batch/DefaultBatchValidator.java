package com.rideaustin.service.batch;

import java.lang.reflect.Field;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

import com.rideaustin.service.model.DriverBatchUpdateError;

import lombok.extern.slf4j.Slf4j;

@Component
public class DefaultBatchValidator implements BatchFieldValidator {

  private static final Map<Class, BatchFieldValidator> VALIDATOR_CACHE = new ConcurrentHashMap<>();

  @Override
  public Optional<DriverBatchUpdateError> validate(String value, Field field, int rowNumber) {
    BatchFieldValidator validator = VALIDATOR_CACHE.get(field.getType());
    if (validator == null) {
      if (Enum.class.isAssignableFrom(field.getType())) {
        validator = new EnumValidator((Class<? extends Enum>) field.getType());
      } else if (Date.class.isAssignableFrom(field.getType())) {
        validator = new DateValidator();
      } else {
        validator = new NullValidator();
      }
      VALIDATOR_CACHE.put(field.getType(), validator);
    }
    return validator.validate(value, field, rowNumber);
  }

  private static class NullValidator implements BatchFieldValidator {

    @Override
    public Optional<DriverBatchUpdateError> validate(String value, Field field, int rowNumber) {
      return Optional.empty();
    }
  }

  @Slf4j
  private static class EnumValidator implements BatchFieldValidator {

    private final Class<? extends Enum> enumClass;

    private EnumValidator(Class<? extends Enum> enumClass) {
      this.enumClass = enumClass;
    }

    @Override
    public Optional<DriverBatchUpdateError> validate(String value, Field field, int rowNumber) {
      try {
        String rawValue = Optional.ofNullable(value).map(String::trim).map(String::toUpperCase).filter(BatchValidator.notEmpty()).orElse("");
        if (!rawValue.isEmpty()) {
          Enum.valueOf(enumClass, rawValue);
        }
        return Optional.empty();
      } catch (IllegalArgumentException e) {
        log.error("Validation error", e);
        return Optional.of(new DriverBatchUpdateError(rowNumber, field.getName(), value.toUpperCase(), "Invalid value"));
      }
    }
  }

  private static class DateValidator implements BatchFieldValidator {

    private static final String PATTERN = "[0-9]{2}/[0-9]{2}/[0-9]{4}";

    @Override
    public Optional<DriverBatchUpdateError> validate(String value, Field field, int rowNumber) {
      String date = Optional.ofNullable(value).map(String::trim).orElse("");
      if (!date.isEmpty() && !Pattern.compile(PATTERN).matcher(date).matches()) {
        return Optional.of(new DriverBatchUpdateError(rowNumber, field.getName(), value, "Invalid date format. Please provide date in MM/dd/YYYY format."));
      }
      return Optional.empty();
    }
  }
}
