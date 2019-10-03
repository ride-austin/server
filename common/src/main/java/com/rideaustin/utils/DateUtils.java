package com.rideaustin.utils;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.util.Date;

import javax.annotation.Nonnull;

import org.apache.commons.lang3.StringUtils;

import com.rideaustin.Constants;

public final class DateUtils {

  private DateUtils() {
  }

  @Nonnull
  public static Date localDateTimeToDate(@Nonnull LocalDateTime dateTime, ZoneId zoneId) {
    return Date.from(dateTime.atZone(zoneId).toInstant());
  }

  @Nonnull
  public static Date localDateToDate(@Nonnull LocalDate date) {
    return localDateTimeToDate(date.atStartOfDay(), ZoneId.systemDefault());
  }

  @Nonnull
  public static Date localDateToDate(@Nonnull LocalDate date, @Nonnull ZoneId zoneId) {
    return localDateTimeToDate(date.atStartOfDay(), zoneId);
  }

  @Nonnull
  public static Instant dateToInstant(@Nonnull Date date) {
    return Instant.ofEpochMilli(date.getTime());
  }

  @Nonnull
  public static Date getDateStringWithOffset(Date date, String zoneOffset) {
    String offsetValue = zoneOffset;
    if (StringUtils.isEmpty(offsetValue)) {
      offsetValue = "+00:00";
    }
    return Date.from(
      ZonedDateTime.ofInstant(date.toInstant(), ZoneId.of("UTC"))
        .withZoneSameInstant(ZoneOffset.of(offsetValue))
        .truncatedTo(ChronoUnit.DAYS)
        .toInstant()
    );
  }

  public static LocalDate getEndOfWeek(Date date) {
    return getEndOfWeek(date.toInstant());
  }

  public static LocalDate getEndOfWeek(Instant instant) {
    return LocalDateTime.ofInstant(instant, Constants.CST_ZONE).with(DayOfWeek.SUNDAY).toLocalDate();
  }

  public static Instant getEndOfTheDay(Instant day) {
    if (day == null) {
      return null;
    }
    return day.atZone(Constants.CST_ZONE).toLocalDate()
      .atStartOfDay().plusDays(1L).minusSeconds(1L).atZone(Constants.CST_ZONE).toInstant();
  }

  public static boolean isWithinHours(Date date, int from, int to) {
    final int currentHour = LocalDateTime.ofInstant(dateToInstant(date), ZoneId.of("UTC"))
      .get(ChronoField.HOUR_OF_DAY);
    if (from <= currentHour && currentHour < to) {
      return true;
    }
    return from > to && (from <= currentHour || currentHour < to);
  }

}
