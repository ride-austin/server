package com.rideaustin.report;

import static org.junit.Assert.assertEquals;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.Test;
import org.mockito.Mock;

import com.google.common.collect.ImmutableMap;
import com.querydsl.core.Tuple;
import com.rideaustin.Constants;
import com.rideaustin.TupleImpl;
import com.rideaustin.model.reports.TNCTripReportResult;
import com.rideaustin.repo.dsl.RideReportDslRepository;

public class TNCTripReportTest {

  @Mock
  private RideReportDslRepository rideReportDslRepository;

  @Test
  public void extractTimeBlock() throws Exception {
    Map<String, Integer[]> mapping = ImmutableMap.<String, Integer[]>builder()
      .put("2:00AM - 6:00AM", new Integer[]{2, 3, 4, 5})
      .put("6:00AM - 10:00AM", new Integer[]{6, 7, 8, 9})
      .put("10:00AM - 2:00PM", new Integer[]{10, 11, 12, 13})
      .put("2:00PM - 6:00PM", new Integer[]{14, 15, 16, 17})
      .put("6:00PM - 10:00PM", new Integer[]{18, 19, 20, 21})
      .put("10:00PM - 2:00AM", new Integer[]{22, 23, 0, 1})
      .build();
    for (Entry<String, Integer[]> entry : mapping.entrySet()) {
      for (Integer hour : entry.getValue()) {
        TNCTripReportResult result = new TNCTripReportResult(createTuple(hour));
        String actual = TNCCompositeReport.extractTimeBlock(result.getCreatedDate());
        assertEquals(entry.getKey(), actual);
      }
    }
  }

  private Tuple createTuple(int hour) {
    return new TupleImpl(Timestamp.from(LocalDateTime.of(2016, 9, 23, hour, 1, 1).atZone(Constants.CST_ZONE).toInstant()),
      null, null, null, null);
  }

}