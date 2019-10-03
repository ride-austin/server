package com.rideaustin.report;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.Test;
import org.mockito.Mock;

import com.google.common.collect.ImmutableList;
import com.rideaustin.Constants;
import com.rideaustin.repo.dsl.DriverReportDslRepository;
import com.rideaustin.repo.dsl.RideReportDslRepository;
import com.rideaustin.report.entry.SurgeHistoryEntry;
import com.rideaustin.report.entry.TNCSummaryReportEntry;
import com.rideaustin.report.params.TNCCompositeReportParams;

public class TNCSummaryReportTest extends AbstractReportTest<TNCSummaryReport> {

  @Mock
  private RideReportDslRepository rideReportDslRepository;
  @Mock
  private DriverReportDslRepository driverReportDslRepository;

  @Override
  protected TNCSummaryReport doCreateTestedInstance() {
    return new TNCSummaryReport(rideReportDslRepository, driverReportDslRepository);
  }

  @Test
  public void doExecute() throws Exception {
    testedInstance.setParameters("{\"startDate\":\"2016-07-01T00:05:00.000Z\",\"endDate\":\"2016-08-01T00:05:00.000Z\"}", TNCCompositeReportParams.class);
    when(rideReportDslRepository.getTNCSummary(any(), any())).thenReturn(new TNCSummaryReportEntry());
    when(rideReportDslRepository.getSurgeAreaHistory(any(), any())).thenReturn(createTimeSpansList());
    testedInstance.doExecute();

    TNCSummaryReportEntry result = testedInstance.getResultsStream().findFirst().orElse(null);

    assertNotNull(result);
    assertEquals(BigDecimal.valueOf(6840).divide(Constants.SECONDS_PER_HOUR, 2, Constants.ROUNDING_MODE), result.getSurgePricingEffect2am6am());
    assertEquals(BigDecimal.valueOf(14400).divide(Constants.SECONDS_PER_HOUR, 2, Constants.ROUNDING_MODE), result.getSurgePricingEffect6am10am());
    assertEquals(BigDecimal.valueOf(14400).divide(Constants.SECONDS_PER_HOUR, 2, Constants.ROUNDING_MODE), result.getSurgePricingEffect10am2pm());
    assertEquals(BigDecimal.valueOf(7812).divide(Constants.SECONDS_PER_HOUR, 2, Constants.ROUNDING_MODE), result.getSurgePricingEffect2pm6pm());
  }

  private List<SurgeHistoryEntry> createTimeSpansList() {

    return ImmutableList.of(
      new SurgeHistoryEntry(date(2016, 9, 22, 3, 52, 30), BigDecimal.ONE),
      new SurgeHistoryEntry(date(2016, 9, 22, 4, 3, 42), BigDecimal.ONE),
      new SurgeHistoryEntry(date(2016, 9, 22, 4, 4, 14), BigDecimal.valueOf(1.25)),
      new SurgeHistoryEntry(date(2016, 9, 22, 4, 4, 27), BigDecimal.valueOf(1.25)),
      new SurgeHistoryEntry(date(2016, 9, 22, 4, 6, 8), BigDecimal.valueOf(1.25)),
      new SurgeHistoryEntry(date(2016, 9, 22, 4, 6, 38), BigDecimal.ONE),
      new SurgeHistoryEntry(date(2016, 9, 22, 4, 7, 24), BigDecimal.ONE),
      new SurgeHistoryEntry(date(2016, 9, 22, 4, 7, 31), BigDecimal.ONE),
      new SurgeHistoryEntry(date(2016, 9, 22, 4, 8, 17), BigDecimal.valueOf(1.5)),
      new SurgeHistoryEntry(date(2016, 9, 22, 4, 9, 12), BigDecimal.valueOf(1.5)),
      new SurgeHistoryEntry(date(2016, 9, 22, 16, 10, 17), BigDecimal.valueOf(1.5))
    );
  }

  private Instant date(int year, int month, int dayOfMonth, int hour, int minute, int second) {
    return LocalDateTime.of(year, month, dayOfMonth, hour, minute, second).atZone(Constants.CST_ZONE).toInstant();
  }

}