package com.rideaustin.utils;

import java.io.IOException;

import org.joda.money.Money;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

public class MoneySerializer extends JsonSerializer<Money> {
  @Override
  public void serialize(Money value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
    jgen.writeString(value.getAmount().toPlainString());
  }
}
