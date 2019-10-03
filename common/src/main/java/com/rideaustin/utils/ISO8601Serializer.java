package com.rideaustin.utils;

import java.io.IOException;
import java.util.Date;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.util.ISO8601DateFormat;

public class ISO8601Serializer extends JsonSerializer<Date> {

  private static final ISO8601DateFormat DATE_FORMAT = new ISO8601DateFormat();

  @Override
  public void serialize(Date value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
    gen.writeString(DATE_FORMAT.format(value));
  }
}
