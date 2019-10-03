package com.rideaustin.utils;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.lang3.time.DurationFormatUtils;
import org.joda.money.Money;

import com.rideaustin.Constants;

public class FormatUtils {

  private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMMM dd, yyyy");
  private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("hh:mma");

  private FormatUtils(){}

  @Nonnull
  public static String formatDate(@Nonnull Date date) {
    return DATE_FORMATTER.format(DateUtils.dateToInstant(date).atZone(Constants.CST_ZONE));
  }

  @Nonnull
  public static String formatTime(@Nonnull Date date) {
    return TIME_FORMATTER.format(DateUtils.dateToInstant(date).atZone(Constants.CST_ZONE));
  }

  public static String formatDateTime(@Nonnull Date date) {
    return String.format("%s %s", formatDate(date), formatTime(date));
  }

  @Nonnull
  public static String formatMoneyAmount(@Nullable Money value) {
    if (value == null || value.getAmount() == null) {
      return formatDecimal(BigDecimal.ZERO);
    }
    return formatDecimal(value.getAmount());
  }

  @Nonnull
  public static String formatDecimal(@Nullable BigDecimal amount) {
    if (amount == null) {
      return "";
    }
    return amount.setScale(2, Constants.ROUNDING_MODE).toString();
  }

  @Nonnull
  public static String formatDuration(@Nonnull Date from, @Nonnull Date to) {
    return formatDuration(from, to, "HH:mm:ss");
  }

  @Nonnull
  public static String formatDuration(@Nonnull Date from, @Nonnull Date to, @Nonnull String pattern) {
    return DurationFormatUtils.formatDuration(to.getTime() - from.getTime(), pattern);
  }

}
