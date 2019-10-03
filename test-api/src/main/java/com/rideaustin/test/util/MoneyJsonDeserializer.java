package com.rideaustin.test.util;

import java.io.IOException;
import java.math.BigDecimal;

import org.joda.money.CurrencyUnit;
import org.joda.money.Money;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.rideaustin.Constants;

public class MoneyJsonDeserializer extends StdDeserializer<Money> {
  public MoneyJsonDeserializer() {
    super(Money.class);
  }

  @Override
  public Money deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
    return Money.of(CurrencyUnit.USD, jsonParser.readValueAs(BigDecimal.class), Constants.ROUNDING_MODE);
  }
}
