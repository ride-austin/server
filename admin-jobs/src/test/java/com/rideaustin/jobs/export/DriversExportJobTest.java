package com.rideaustin.jobs.export;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Collections;

import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.mail.EmailException;
import org.junit.Test;
import org.mockito.Mock;
import org.springframework.core.env.Environment;

import com.google.common.collect.ImmutableSet;
import com.rideaustin.jobs.GenericReportJob;
import com.rideaustin.jobs.GenericReportJobBaseTest;
import com.rideaustin.model.City;
import com.rideaustin.model.enums.PayoneerStatus;
import com.rideaustin.report.DriversExportReport;
import com.rideaustin.report.model.ReportMetadata;
import com.rideaustin.report.model.ReportParameter;
import com.rideaustin.report.model.ReportParameterType;
import com.rideaustin.report.params.DriversExportReportParams;
import com.rideaustin.rest.exception.RideAustinException;
import com.rideaustin.rest.model.ListDriversParams;
import com.rideaustin.service.CityService;

public class DriversExportJobTest extends GenericReportJobBaseTest {

  private static final String JOBS_DRIVERS_REPORT_RECIPIENTS = "jobs.drivers_report.recipients";

  @Mock
  private Environment environment;
  @Mock
  private DriversExportReport report;
  @Mock
  private CityService cityService;

  @Override
  protected GenericReportJob createTestedInstance() {
    return new DriversExportJob();
  }

  @Test
  public void executeInternalSetsRecipientsToDefaultIfNoRecipientsWereSet() throws Exception {
    ReportMetadata metadata = createReportMetadata(Collections.emptySet(), RandomUtils.nextLong(0, 100), DriversExportReport.class);
    when(environment.getProperty(JOBS_DRIVERS_REPORT_RECIPIENTS)).thenReturn(GenericReportJobBaseTest.RECIPIENTS);
    setupGenericReportJob(metadata);

    ((DriversExportJob) testedInstance).executeInternal();

    verify(renderer, only()).createEmailFor(eq(report), eq(Collections.singletonList(GenericReportJobBaseTest.RECIPIENTS)), any(City.class));
  }

  @Test
  public void executeInternalSetsParamsToEmptyIfNoParamsWereSet() throws Exception {
    ReportMetadata metadata = createReportMetadata(ImmutableSet.of(
      new ReportParameter("", "", "", ReportParameterType.STRING, false, false, "", 0, null)), RandomUtils.nextLong(0, 100), DriversExportReport.class);
    setupGenericReportJob(metadata);

    when(environment.getProperty(JOBS_DRIVERS_REPORT_RECIPIENTS)).thenReturn(GenericReportJobBaseTest.RECIPIENTS);

    ((DriversExportJob) testedInstance).executeInternal();

    verify(report).setParameters(eq("{}"), eq(DriversExportReportParams.class));
  }

  @Test
  public void executeInternalConvertsParamsFromListDriversParams() throws Exception {
    // preconditions
    ReportMetadata metadata = createReportMetadata(ImmutableSet.of(
      new ReportParameter("", "", "", ReportParameterType.STRING, false, false, "", 0, null)), RandomUtils.nextLong(0, 100), DriversExportReport.class);
    ListDriversParams params = createListDriversParams();
    String jsonExpected = this.objectMapper.writeValueAsString(new DriversExportReportParams(params));
    setupGenericReportJob(metadata);
    ((DriversExportJob) testedInstance).setParams(params);
    when(environment.getProperty(JOBS_DRIVERS_REPORT_RECIPIENTS)).thenReturn(GenericReportJobBaseTest.RECIPIENTS);

    // test
    ((DriversExportJob) testedInstance).executeInternal();

    // verify
    verify(report).setParameters(eq(jsonExpected), eq(DriversExportReportParams.class));
  }

  private ListDriversParams createListDriversParams() {
    ListDriversParams listDriversParams = new ListDriversParams();
    listDriversParams.setCreatedOnBefore(LocalDateTime.of(2016, 9, 30, 1, 2, 3).toInstant(ZoneOffset.UTC));
    listDriversParams.setCreatedOnAfter(LocalDateTime.of(2016, 8, 30, 1, 2, 3).toInstant(ZoneOffset.UTC));
    listDriversParams.setEnabled(true);
    listDriversParams.setActive(true);
    listDriversParams.setPayoneerStatus(Collections.singletonList(PayoneerStatus.ACTIVE));
    listDriversParams.setCarCategory(Collections.singletonList("REGULAR"));
    listDriversParams.setName("Name");
    listDriversParams.setEmail("email@mail.com");
    listDriversParams.setDriverId(100L);
    return listDriversParams;
  }

  private void setupGenericReportJob(ReportMetadata metadata) throws RideAustinException, EmailException {
    when(reportService.getReportForClass(eq(DriversExportReport.class))).thenReturn(metadata);
    when(beanFactory.getBean(eq(DriversExportReport.class))).thenReturn(report);
    when(reportExecutionService.execute(eq(report))).thenReturn(renderer);
    when(renderer.createEmailFor(eq(report), anyList(), any(City.class))).thenReturn(email);
    when(cityService.getDefaultCity()).thenReturn(generateCity());
  }

  public City generateCity() {
    City city = new City();
    city.setAppName("RideAustin");
    city.setOffice("Office Address");
    return city;
  }
}