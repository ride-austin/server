package com.rideaustin.report;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Optional;

import org.joda.money.Money;

import com.querydsl.core.Tuple;
import com.rideaustin.Constants;

public interface TupleConsumer {

  default Long getLong(Tuple tuple, int index) {
    return get(tuple, index, Long.class);
  }

  default Integer getInteger(Tuple tuple, int index) {
    return get(tuple, index, Integer.class);
  }

  default String getString(Tuple tuple, int index) {
    return get(tuple, index, String.class);
  }

  default LocalDateTime getLocalDateTimeFromTimestamp(Tuple tuple, int index) {
    return Optional.ofNullable(get(tuple, index, Timestamp.class))
      .map(d ->
        d.toLocalDateTime().atZone(Constants.CST_ZONE).toLocalDateTime()).orElse(null);
  }

  default Instant getInstantFromTimestamp(Tuple tuple, int index) {
    return Optional.ofNullable(get(tuple, index, Timestamp.class)).map(Timestamp::toInstant).orElse(null);
  }

  default Instant getInstantFromDate(Tuple tuple, int index) {
    return Optional.ofNullable(tuple.get(index, java.sql.Date.class))
      .map(d -> d.toLocalDate().atStartOfDay().atZone(Constants.CST_ZONE).toInstant()).orElse(null);
  }

  default Date getDate(Tuple tuple, int index) {
    return get(tuple, index, Date.class);
  }

  default Double getDouble(Tuple tuple, int index) {
    return get(tuple, index, Double.class);
  }

  default Money getMoney(Tuple tuple, int index) {
    return get(tuple, index, Money.class);
  }

  default BigDecimal getBigDecimal(Tuple tuple, int index) {
    return get(tuple, index, BigDecimal.class);
  }

  default Boolean getBoolean(Tuple tuple, int index) {
    return get(tuple, index, Boolean.class);
  }

  default <T> T get(Tuple tuple, int index, Class<T> clazz) {
    return tuple.get(index, clazz);
  }
}
