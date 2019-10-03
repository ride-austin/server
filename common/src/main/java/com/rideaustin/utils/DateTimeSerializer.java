package com.rideaustin.utils;

import java.io.IOException;
import java.util.Date;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.rideaustin.Constants;

public class DateTimeSerializer extends JsonSerializer<Date> {
  @Override
  public void serialize(Date date, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
    jsonGenerator.writeString(Constants.DATETIME_FORMATTER.format(DateUtils.dateToInstant(date)));
  }
}
