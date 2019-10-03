package com.rideaustin.utils;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.fasterxml.jackson.core.JsonGenerator;
import com.rideaustin.Constants;

public class DateTimeSerializerTest {

  @Mock
  private JsonGenerator jsonGenerator;

  private DateTimeSerializer testedInstance;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);

    testedInstance = new DateTimeSerializer();
  }

  @Test
  public void serializeAccordingToFormat() throws IOException {
    Date date = new Date();
    final String expected = Constants.DATETIME_FORMATTER.format(DateUtils.dateToInstant(date));

    testedInstance.serialize(date, jsonGenerator, null);

    verify(jsonGenerator, times(1)).writeString(eq(expected));
  }
}