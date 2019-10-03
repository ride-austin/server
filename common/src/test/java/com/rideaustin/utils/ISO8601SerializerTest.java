package com.rideaustin.utils;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.fasterxml.jackson.core.JsonGenerator;

public class ISO8601SerializerTest {

  @Mock
  private JsonGenerator generator;

  private ISO8601Serializer testedInstance;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);

    testedInstance = new ISO8601Serializer();
  }

  @Test
  public void serializeWritesFormattedDate() throws IOException {
    final Date date = DateUtils.localDateTimeToDate(LocalDateTime.of(2019, 12, 31, 23, 59, 59), ZoneId.of("UTC"));

    testedInstance.serialize(date, generator, null);

    verify(generator, times(1)).writeString(eq("2019-12-31T23:59:59Z"));
  }
}