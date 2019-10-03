package com.rideaustin.utils;

import java.io.IOException;
import java.math.BigDecimal;

import org.joda.money.CurrencyUnit;
import org.joda.money.Money;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

public class MoneyDeserializer extends JsonDeserializer<Money> {
  @Override
  public Money deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
    return Money.of(CurrencyUnit.USD, jsonParser.readValueAs(BigDecimal.class));
  }
}
