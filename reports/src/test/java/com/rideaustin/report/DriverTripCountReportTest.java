package com.rideaustin.report;

import static java.util.stream.Collectors.toList;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.RandomUtils;
import org.junit.Test;
import org.mockito.Mock;

import com.rideaustin.TupleImpl;
import com.rideaustin.model.reports.AvatarTripCountReportResultEntry;
import com.rideaustin.repo.dsl.RideReportDslRepository;
import com.rideaustin.report.entry.DriverTripCountReportEntry;
import com.rideaustin.report.params.DriverTripCountReportParams;
import com.rideaustin.utils.RandomString;

public class DriverTripCountReportTest extends AbstractReportTest<DriverTripCountReport> {

  @Mock
  private RideReportDslRepository reportRepository;

  @Override
  protected DriverTripCountReport doCreateTestedInstance() {
    return new DriverTripCountReport(reportRepository);
  }

  @Test
  public void doExecute() throws Exception {
    when(reportRepository.getDriverTripsRaw(any(Instant.class))).thenReturn(
      Arrays.asList(
        createEntry(1L, Instant.parse("2016-12-19T12:05:00.000Z")),
        createEntry(2L, Instant.parse("2016-12-19T12:05:00.000Z")),
        createEntry(1L, Instant.parse("2016-12-27T12:05:00.000Z")),
        createEntry(1L, Instant.parse("2016-12-27T12:05:00.000Z"))
    ));

    testedInstance.setParameters("{\"completedOnBefore\":\"2016-12-28T00:00:05.000Z\"}", DriverTripCountReportParams.class);
    testedInstance.doExecute();

    List<DriverTripCountReportEntry> result = testedInstance.resultsStream.collect(toList());

    assertEquals(3, result.size());
    assertEquals(Long.valueOf(1L), result.get(0).getDriverId());
    assertEquals(Long.valueOf(2L), result.get(1).getDriverId());
    assertEquals(Long.valueOf(1L), result.get(2).getDriverId());
  }

  private AvatarTripCountReportResultEntry createEntry(long id, Instant completedOn) {
    return new AvatarTripCountReportResultEntry(new TupleImpl(RandomUtils.nextLong(1L, 100L), id, Timestamp.from(completedOn),
      RandomString.generate(), RandomString.generate(), RandomString.generate()));
  }

  @Test
  public void setParametersSetsEndDateToEndOfDay() throws Exception {
    testedInstance.setParameters("{\"completedOnBefore\":\"2016-10-24T00:00:05.000Z\"}", DriverTripCountReportParams.class);

    assertEquals(LocalDateTime.of(2016,10,24,4,59,59).toInstant(ZoneOffset.UTC), testedInstance.parameters.getCompletedOnBefore());
  }

}