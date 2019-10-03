package com.rideaustin.report;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAccessor;
import java.util.Date;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.joda.money.Money;

import com.google.common.collect.ImmutableMap;
import com.rideaustin.Constants;
import com.rideaustin.model.enums.RideStatus;

public enum FormatAs {

  NONE {
    @Override
    public String toString(Object value) {
      return value.toString();
    }
  },
  NUMERIC {
    @Override
    public String toString(Object value) {
      return Optional.ofNullable(value).map(Objects::toString).orElse("");
    }
  },
  STRING {
    @Override
    public String toString(Object value) {
      return (String) value;
    }
  },
  DATE {
    @Override
    public String toString(Object value) {
      Optional<TemporalAccessor> dateValue;
      if (value instanceof TemporalAccessor) {
        dateValue = Optional.ofNullable((TemporalAccessor) value);
      } else {
        dateValue = Optional.ofNullable((Date) value).map(Date::toInstant);
      }
      return dateValue.map(Constants.DATE_FORMATTER::format).orElse("");
    }
  },
  DATETIME {
    @Override
    public String toString(Object value) {
      return Optional.ofNullable((TemporalAccessor) value).map(Constants.DATETIME_FORMATTER::format).orElse("");
    }
  },
  MONEY {
    @Override
    public String toString(Object value) {
      return Optional.ofNullable((Money) value)
        .map(Money::getAmount)
        .map(String::valueOf)
        .orElse("");
    }
  },
  BOOLEAN {
    @Override
    public String toString(Object value) {
      return Optional.ofNullable((Boolean) value).map(Objects::toString).orElse("");
    }
  },
  YES_NO {
    @Override
    public String toString(Object value) {
      return Optional.ofNullable((Boolean) value).map(e -> e ? "yes" : "no").orElse("");
    }
  },
  RIDE_STATUS {
    @Override
    public String toString(Object value) {
      switch ((RideStatus) value) {
        case COMPLETED:
          return "Completed";
        case RIDER_CANCELLED:
          return "Cancelled by rider";
        case DRIVER_CANCELLED:
          return "Cancelled by driver";
        case NO_AVAILABLE_DRIVER:
          return "Driver not assigned";
        default:
          return "Other";
      }
    }
  };

  private static final Map<Class, FormatAs> DEFAULT_FORMAT = ImmutableMap.<Class, FormatAs>builder()
    .put(Integer.class, NUMERIC)
    .put(Long.class, NUMERIC)
    .put(Double.class, NUMERIC)
    .put(BigDecimal.class, NUMERIC)
    .put(String.class, STRING)
    .put(Instant.class, DATETIME)
    .put(Date.class, DATE)
    .put(LocalDate.class, DATE)
    .put(LocalDateTime.class, DATETIME)
    .put(Money.class, MONEY)
    .put(Boolean.class, BOOLEAN)
    .put(RideStatus.class, RIDE_STATUS)
    .build();

  public abstract String toString(Object value);

  public static FormatAs formatFor(Class clazz) {
    return DEFAULT_FORMAT.get(clazz);
  }
}
