package com.rideaustin.utils;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

import org.junit.Test;

public class DateUtilsTest {

  @Test
  public void testPassFromIsLessThanTo() {
    Date date = DateUtils.localDateTimeToDate(LocalDateTime.of(2018, 1, 1, 13, 0, 0), ZoneId.of("UTC"));
    int from = 10;
    int to = 14;

    assertTrue(DateUtils.isWithinHours(date, from, to));
  }

  @Test
  public void testFailFromIsLessThanTo() {
    Date date = DateUtils.localDateTimeToDate(LocalDateTime.of(2018, 1, 1, 15, 0, 0), ZoneId.of("UTC"));
    int from = 10;
    int to = 14;

    assertFalse(DateUtils.isWithinHours(date, from, to));
  }

  @Test
  public void testPassFromIsGreaterThanToAndTimeIsOnFromSide() {
    Date date = DateUtils.localDateTimeToDate(LocalDateTime.of(2018, 1, 1, 15, 0, 0), ZoneId.of("UTC"));
    int from = 10;
    int to = 2;

    assertTrue(DateUtils.isWithinHours(date, from, to));
  }

  @Test
  public void testPassFromIsGreaterThanToAndTimeIsOnToSide() {
    Date date = DateUtils.localDateTimeToDate(LocalDateTime.of(2018, 1, 1, 1, 0, 0), ZoneId.of("UTC"));
    int from = 10;
    int to = 2;

    assertTrue(DateUtils.isWithinHours(date, from, to));
  }

  @Test
  public void testFailFromIsGreaterThanToAndTimeIsOnFromSide() {
    Date date = DateUtils.localDateTimeToDate(LocalDateTime.of(2018, 1, 1, 9, 0, 0), ZoneId.of("UTC"));
    int from = 10;
    int to = 2;

    assertFalse(DateUtils.isWithinHours(date, from, to));
  }

  @Test
  public void testFailFromIsGreaterThanToAndTimeIsOnToSide() {
    Date date = DateUtils.localDateTimeToDate(LocalDateTime.of(2018, 1, 1, 3, 0, 0), ZoneId.of("UTC"));
    int from = 10;
    int to = 2;

    assertFalse(DateUtils.isWithinHours(date, from, to));
  }
}