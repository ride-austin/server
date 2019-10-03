package com.rideaustin.report.adapter;

import static org.junit.Assert.assertArrayEquals;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;

import org.joda.money.CurrencyUnit;
import org.joda.money.Money;
import org.junit.Before;
import org.junit.Test;

import com.rideaustin.Constants;

public class DefaultReportAdapterTest {

  private DefaultReportAdapter<Entry> testedInstance;

  @Before
  public void setUp() throws Exception {
    testedInstance = new DefaultReportAdapter<>(Entry.class, Collections.emptyMap());
  }

  @Test
  public void getRowMapper() throws Exception {
    Entry entry = new Entry();
    Instant now = Instant.now();
    entry.setDate(now);
    entry.setDatetime(now);
    entry.setIntegerField(500);
    entry.setMoney(Money.of(CurrencyUnit.USD, BigDecimal.ONE));
    entry.setNumeric(5.5);
    entry.setString("Hello");

    String[] expected = new String[]{
      "Hello",
      "500",
      "5.5",
      String.valueOf(Money.of(CurrencyUnit.USD, BigDecimal.ONE).getAmount()),
      Constants.DATE_FORMATTER.format(now),
      Constants.DATETIME_FORMATTER.format(now),
      "2500",
      "5000",
      "0"
    };

    String[] actual = testedInstance.getRowMapper().apply(entry);

    assertArrayEquals(expected, actual);
  }

  @Test
  public void testGetHeaders() throws Exception {
    String[] expected = new String[] {
      "String",
      "Integer field",
      "Numeric",
      "cash",
      "Date",
      "Datetime",
      "Virtual",
      "Virtual ten",
      "zero"
    };

    String[] actual = testedInstance.getHeaders();

    assertArrayEquals(expected, actual);
  }

}