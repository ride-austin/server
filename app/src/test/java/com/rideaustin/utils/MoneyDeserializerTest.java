package com.rideaustin.utils;

import static com.rideaustin.test.util.TestUtils.money;
import static org.junit.Assert.*;
import static org.powermock.api.mockito.PowerMockito.when;

import java.io.IOException;
import java.math.BigDecimal;

import org.joda.money.Money;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.fasterxml.jackson.core.JsonParser;

public class MoneyDeserializerTest {

  @Mock
  private JsonParser jsonParser;

  private MoneyDeserializer testedInstance;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);

    testedInstance = new MoneyDeserializer();
  }

  @Test
  public void deserializeCreatesMoneyInstance() throws IOException {
    when(jsonParser.readValueAs(BigDecimal.class)).thenReturn(BigDecimal.TEN);

    final Money result = testedInstance.deserialize(jsonParser, null);

    assertEquals(money(10.0), result);
  }
}