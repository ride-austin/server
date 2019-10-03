package com.rideaustin.utils;

import static com.rideaustin.test.util.TestUtils.money;
import static org.junit.Assert.*;
import static org.mockito.Mockito.verify;

import java.io.IOException;

import org.joda.money.Money;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.fasterxml.jackson.core.JsonGenerator;

public class MoneySerializerTest {

  @Mock
  private JsonGenerator jsonGenerator;

  private MoneySerializer testedInstance;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);

    testedInstance = new MoneySerializer();
  }

  @Test
  public void serializeWritesNumber() throws IOException {
    final Money money = money(10.0);

    testedInstance.serialize(money, jsonGenerator, null);

    verify(jsonGenerator).writeString("10.00");
  }
}