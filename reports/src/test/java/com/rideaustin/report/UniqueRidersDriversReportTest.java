package com.rideaustin.report;

import static java.util.stream.Collectors.toList;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.google.common.collect.ImmutableMap;
import com.rideaustin.repo.dsl.UniqueRidersDriversReportRepository;
import com.rideaustin.report.entry.UniqueRidersDriversReportEntry;

public class UniqueRidersDriversReportTest {

  @Mock
  private UniqueRidersDriversReportRepository reportRepository;

  private UniqueRidersDriversReport testedInstance;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
    testedInstance = new UniqueRidersDriversReport(reportRepository);
  }

  @Test
  public void doExecute() throws Exception {
    when(reportRepository.findDriversSignedUpByWeek()).thenReturn(ImmutableMap.of(
      LocalDate.of(2016, 10, 9), 3L,
      LocalDate.of(2016, 10, 16), 3L,
      LocalDate.of(2016, 10, 23), 4L
    ));
    when(reportRepository.findRidersSignedUpByWeek()).thenReturn(ImmutableMap.of(
      LocalDate.of(2016, 10, 9), 7L,
      LocalDate.of(2016, 10, 16), 2L,
      LocalDate.of(2016, 10, 23), 1L
    ));
    when(reportRepository.sumOnlineHours()).thenReturn(ImmutableMap.of(
      LocalDate.of(2016, 10, 9), BigDecimal.valueOf(30.0),
      LocalDate.of(2016, 10, 16), BigDecimal.valueOf(30.0),
      LocalDate.of(2016, 10, 23), BigDecimal.valueOf(40.0)
    ));
    when(reportRepository.sumDrivenHours()).thenReturn(ImmutableMap.of(
      LocalDate.of(2016, 10, 9), BigDecimal.valueOf(40.0),
      LocalDate.of(2016, 10, 16), BigDecimal.valueOf(30.0),
      LocalDate.of(2016, 10, 23), BigDecimal.valueOf(20.0)
    ));

    testedInstance.doExecute();

    List<UniqueRidersDriversReportEntry> result = testedInstance.resultsStream.collect(toList());

    assertEquals(3, result.size());
    assertEntry(LocalDate.of(2016, 10, 9), 3L, 3L, 7L, 7L, BigDecimal.valueOf(30.0), BigDecimal.valueOf(40.0), result.get(0));
    assertEntry(LocalDate.of(2016, 10, 16), 6L, 3L, 9L, 2L, BigDecimal.valueOf(30.0), BigDecimal.valueOf(30.0), result.get(1));
    assertEntry(LocalDate.of(2016, 10, 23), 10L, 4L, 10L, 1L, BigDecimal.valueOf(40.0), BigDecimal.valueOf(20.0), result.get(2));
  }

  private void assertEntry(LocalDate endOfWeek, Long uniqueDrivers, Long firstTimeDrivers, Long uniqueRiders,
    Long firstTimeRiders, BigDecimal online, BigDecimal driven, UniqueRidersDriversReportEntry entry) {

    assertEquals(endOfWeek, entry.getEndOfWeek());
    assertEquals(uniqueDrivers, entry.getTotalDriversSignedUp());
    assertEquals(firstTimeDrivers, entry.getDriversSignedUpThisWeek());
    assertEquals(uniqueRiders, entry.getTotalRidersSignedUp());
    assertEquals(firstTimeRiders, entry.getRidersSignedUpThisWeek());
    assertEquals(online, entry.getHoursOnline());
    assertEquals(driven, entry.getHoursDriven());
  }

}