package com.rideaustin.report;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Test;
import org.mockito.Mock;

import com.rideaustin.model.enums.RideStatus;
import com.rideaustin.repo.dsl.RideReportDslRepository;
import com.rideaustin.report.entry.TripFulfillmentReportEntry;
import com.rideaustin.report.params.TripFulfillmentReportParams;
import com.rideaustin.model.reports.TripFulfillmentQueryResultEntry;

public class TripFulfillmentReportTest extends AbstractReportTest<TripFulfillmentReport> {

  @Mock
  private RideReportDslRepository rideReportDslRepository;

  @Override
  protected TripFulfillmentReport doCreateTestedInstance() {
    return new TripFulfillmentReport(rideReportDslRepository);
  }

  @Test
  public void testDoExecuteWithoutInterval() throws Exception {
    testedInstance.setParameters("{\"startDateTime\":\"2016-07-01T00:05:00.000Z\",\"endDateTime\":\"2016-08-01T00:05:00.000Z\"}", TripFulfillmentReportParams.class);
    when(rideReportDslRepository.getTripFulfillmentReport(any(Instant.class), any(Instant.class)))
      .thenReturn(Arrays.asList(
        new TripFulfillmentQueryResultEntry(
          LocalDateTime.of(2016, 7, 1, 10, 10, 0).toInstant(ZoneOffset.UTC),
          LocalDateTime.of(2016, 7, 1, 11, 10, 0).toInstant(ZoneOffset.UTC),
          null,
          RideStatus.COMPLETED),
        new TripFulfillmentQueryResultEntry(
          LocalDateTime.of(2016, 7, 1, 10, 10, 0).toInstant(ZoneOffset.UTC),
          null,
          LocalDateTime.of(2016, 7, 1, 11, 10, 0).toInstant(ZoneOffset.UTC),
          RideStatus.RIDER_CANCELLED),
        new TripFulfillmentQueryResultEntry(
          LocalDateTime.of(2016, 7, 1, 10, 10, 0).toInstant(ZoneOffset.UTC),
          null,
          LocalDateTime.of(2016, 7, 1, 11, 10, 0).toInstant(ZoneOffset.UTC),
          RideStatus.ADMIN_CANCELLED),
        new TripFulfillmentQueryResultEntry(
          LocalDateTime.of(2016, 7, 1, 10, 10, 0).toInstant(ZoneOffset.UTC),
          null,
          LocalDateTime.of(2016, 7, 1, 11, 10, 0).toInstant(ZoneOffset.UTC),
          RideStatus.DRIVER_CANCELLED),
        new TripFulfillmentQueryResultEntry(
          LocalDateTime.of(2016, 7, 1, 10, 10, 0).toInstant(ZoneOffset.UTC),
          null,
          null,
          RideStatus.NO_AVAILABLE_DRIVER)
      ));

    testedInstance.doExecute();

    List<TripFulfillmentReportEntry> result = testedInstance.getResultsStream().collect(Collectors.toList());

    assertEquals(1, result.size());
    assertEquals(1, result.get(0).getCompleted());
    assertEquals(1, result.get(0).getAdminCancelled());
    assertEquals(1, result.get(0).getDriverCancelled());
    assertEquals(1, result.get(0).getNoDriverAvailable());
    assertEquals(1, result.get(0).getRiderCancelled());
  }

  @Test
  public void testDoExecuteWithInterval() throws Exception {
    testedInstance.setParameters("{\"startDateTime\":\"2016-07-01T00:05:00.000Z\",\"endDateTime\":\"2016-08-01T00:05:00.000Z\",\"interval\":1440}", TripFulfillmentReportParams.class);
    when(rideReportDslRepository.getTripFulfillmentReport(any(Instant.class), any(Instant.class)))
      .thenReturn(Arrays.asList(
        new TripFulfillmentQueryResultEntry(
          LocalDateTime.of(2016, 7, 1, 10, 10, 0).toInstant(ZoneOffset.UTC),
          LocalDateTime.of(2016, 7, 1, 11, 10, 0).toInstant(ZoneOffset.UTC),
          null,
          RideStatus.COMPLETED),
        new TripFulfillmentQueryResultEntry(
          LocalDateTime.of(2016, 7, 2, 10, 10, 0).toInstant(ZoneOffset.UTC),
          LocalDateTime.of(2016, 7, 2, 11, 10, 0).toInstant(ZoneOffset.UTC),
          null,
          RideStatus.COMPLETED),
        new TripFulfillmentQueryResultEntry(
          LocalDateTime.of(2016, 7, 2, 10, 10, 0).toInstant(ZoneOffset.UTC),
          null,
          LocalDateTime.of(2016, 7, 2, 11, 10, 0).toInstant(ZoneOffset.UTC),
          RideStatus.RIDER_CANCELLED),
        new TripFulfillmentQueryResultEntry(
          LocalDateTime.of(2016, 7, 4, 10, 10, 0).toInstant(ZoneOffset.UTC),
          null,
          LocalDateTime.of(2016, 7, 4, 11, 10, 0).toInstant(ZoneOffset.UTC),
          RideStatus.ADMIN_CANCELLED),
        new TripFulfillmentQueryResultEntry(
          LocalDateTime.of(2016, 7, 5, 10, 10, 0).toInstant(ZoneOffset.UTC),
          null,
          LocalDateTime.of(2016, 7, 5, 11, 10, 0).toInstant(ZoneOffset.UTC),
          RideStatus.DRIVER_CANCELLED),
        new TripFulfillmentQueryResultEntry(
          LocalDateTime.of(2016, 7, 6, 10, 10, 0).toInstant(ZoneOffset.UTC),
          null,
          null,
          RideStatus.NO_AVAILABLE_DRIVER)
      ));

    testedInstance.doExecute();

    List<TripFulfillmentReportEntry> result = testedInstance.getResultsStream().collect(Collectors.toList());

    assertEquals(5, result.size());
    assertEquals(1, result.get(0).getCompleted());
    assertEquals(1, result.get(1).getCompleted());
    assertEquals(1, result.get(1).getRiderCancelled());
    assertEquals(1, result.get(2).getAdminCancelled());
    assertEquals(1, result.get(3).getDriverCancelled());
    assertEquals(1, result.get(4).getNoDriverAvailable());
  }
}