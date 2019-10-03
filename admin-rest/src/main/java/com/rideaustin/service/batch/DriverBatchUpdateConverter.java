package com.rideaustin.service.batch;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.google.common.base.CaseFormat;
import com.google.common.collect.ImmutableMap;
import com.rideaustin.Constants;
import com.rideaustin.service.model.DriverBatchUpdateError;
import com.rideaustin.utils.DateUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class DriverBatchUpdateConverter {

  private Map<? extends Class<? extends BatchFieldValidator>, BatchFieldValidator> fieldValidators;
  private Collection<BatchRecordValidator> recordValidators;

  private static final Map<Class<?>, Function<String, ?>> CONVERTERS = ImmutableMap.<Class<?>, Function<String, ?>>builder()
    .put(Integer.class, Integer::valueOf)
    .put(Long.class, Long::valueOf)
    .put(Double.class, Double::valueOf)
    .put(Date.class, s -> DateUtils.localDateToDate(LocalDate.parse(s, Constants.DATE_FORMATTER)))
    .put(String.class, Function.identity())
    .put(Boolean.class, s -> "1".equals(s) || Boolean.parseBoolean(s))
    .put(Set.class, s -> Arrays.stream(s.split(",")).map(String::trim).filter(BatchValidator.notEmpty()).collect(Collectors.toSet()))
    .put(Constants.City.class, s -> {
      try {
        Long id = Long.valueOf(s);
        return Arrays.stream(Constants.City.values()).filter(c -> c.getId().equals(id)).findFirst().orElse(null);
      } catch (NumberFormatException e) {
        return Constants.City.valueOf(s);
      }
    })
    .build();

  public Pair<DriverBatchUpdateDto, List<DriverBatchUpdateError>> convert(String[] header, int rowNumber, String[] row) {
    DriverBatchUpdateDto result = new DriverBatchUpdateDto();
    List<DriverBatchUpdateError> errors = new ArrayList<>();
    Object value = null;
    for (int i = 0; i < header.length; i++) {
      String fieldName = CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, header[i]);
      try {
        Field field = DriverBatchUpdateDto.class.getDeclaredField(fieldName);
        if (!isFieldValid(row[i], i, field, errors)) {
          break;
        }
        value = convert(field.getType(), row[i]);
        if (value != null) {
          BeanUtils.setProperty(result, fieldName, value);
        }
      } catch (NoSuchFieldException ex) {
        log.error("Unknown field", ex);
        errors.add(new DriverBatchUpdateError(i, fieldName, null, "Unknown field"));
      } catch (IllegalAccessException | InvocationTargetException e) {
        log.error("Failed to set value", e);
        errors.add(new DriverBatchUpdateError(i, fieldName, String.valueOf(value), "Failed to set field value"));
      }
    }
    validateRecord(rowNumber, result, errors);
    return ImmutablePair.of(result, errors);
  }

  @EventListener
  public void initValidators(ContextRefreshedEvent event) {
    if (fieldValidators == null && recordValidators == null) {
      fieldValidators = getValidatorBeans(event, BatchFieldValidator.class);
      recordValidators = getValidatorBeans(event, BatchRecordValidator.class).values();
    }
  }

  private boolean isFieldValid(String value, int i, Field field, List<DriverBatchUpdateError> errors) {
    if (field.isAnnotationPresent(Validate.class)) {
      Validate validateAnnotation = field.getAnnotation(Validate.class);
      for (Class<? extends BatchValidator> validatorClass : validateAnnotation.with()) {
        BatchFieldValidator validator = fieldValidators.get(validatorClass);
        if (validator == null || isErrorPresent(errors, validator.validate(value, field, i))) {
          return false;
        }
      }
    }
    return true;
  }

  private void validateRecord(int rowNumber, DriverBatchUpdateDto result, List<DriverBatchUpdateError> errors) {
    if (errors.isEmpty()) {
      for (BatchRecordValidator recordValidator : recordValidators) {
        if (isErrorPresent(errors, recordValidator.validate(rowNumber, result))) {
          break;
        }
      }
    }
  }

  private boolean isErrorPresent(List<DriverBatchUpdateError> errors, Optional<DriverBatchUpdateError> error) {
    if (error.isPresent()) {
      errors.add(error.get());
      return true;
    }
    return false;
  }

  private Object convert(Class<?> type, String value) {
    if (StringUtils.isNotEmpty(value)) {
      Function<String, ?> converter = CONVERTERS.get(type);
      if (converter != null) {
        return converter.apply(value);
      } else if (Enum.class.isAssignableFrom(type)) {
        return Enum.valueOf((Class<? extends Enum>) type, value.toUpperCase());
      }
    }
    return null;
  }

  private <T> Map<Class<? extends T>, T> getValidatorBeans(ContextRefreshedEvent event, Class<T> clazz) {
    return event.getApplicationContext()
      .getBeansOfType(clazz)
      .entrySet()
      .stream()
      .collect(Collectors.toMap(
        e -> (Class<? extends T>) e.getValue().getClass(),
        Map.Entry::getValue
      ));
  }
}
