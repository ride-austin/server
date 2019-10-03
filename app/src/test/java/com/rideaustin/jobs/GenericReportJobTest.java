package com.rideaustin.jobs;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.google.common.collect.ImmutableSet;
import com.rideaustin.model.City;
import com.rideaustin.report.model.ReportMetadata;
import com.rideaustin.report.model.ReportParameter;
import com.rideaustin.report.AirportRidesReport;
import com.rideaustin.report.model.ReportParameterType;
import com.rideaustin.report.params.AirportRidesReportParams;
import com.rideaustin.rest.exception.NotFoundException;
import com.rideaustin.service.CityService;

public class GenericReportJobTest extends GenericReportJobBaseTest {

  private static final long REPORT_ID = 1L;
  private static final String PARAMS_JSON = "{\"test\":\"value\"}";

  @Mock
  private AirportRidesReport report;
  @Mock
  private CityService cityService;

  @Before
  public void setUp() throws Exception {
    super.setUp();
    testedInstance.setReportId(REPORT_ID);
    testedInstance.setParamsJson(PARAMS_JSON);
    testedInstance.setRecipients(RECIPIENTS_LIST);
  }

  @Override
  protected GenericReportJob createTestedInstance() {
    return new GenericReportJob();
  }

  @Test
  public void testResolveParamsClassName() {
    Class<AirportRidesReport> clazz = AirportRidesReport.class;
    String paramsClassName = testedInstance.resolveParamsClassName(clazz.getName(), clazz);
    assertEquals(AirportRidesReportParams.class.getName(), paramsClassName);
  }

  @Test
  public void testReportIsInitialized() throws Exception {
    ReportMetadata metadata = createReportMetadata(ImmutableSet.of(
      new ReportParameter("", "", "", ReportParameterType.STRING, false, false, "", 0, null)), REPORT_ID, AirportRidesReport.class);
    when(reportService.getReport(eq(REPORT_ID))).thenReturn(metadata);
    when(beanFactory.getBean(eq(AirportRidesReport.class))).thenReturn(report);
    when(reportExecutionService.execute(eq(report))).thenReturn(renderer);
    when(renderer.createEmailFor(eq(report), anyList(), any(City.class))).thenReturn(email);

    testedInstance.executeInternal();

    verify(report, times(1)).setParameters(eq(PARAMS_JSON), eq(AirportRidesReportParams.class));
    verify(report, times(1)).setMetadata(eq(metadata));
  }

  @Test
  public void testExecutionServiceIsCalled() throws Exception {
    ReportMetadata metadata = createReportMetadata(Collections.emptySet(), REPORT_ID, AirportRidesReport.class);
    when(reportService.getReport(eq(REPORT_ID))).thenReturn(metadata);
    when(beanFactory.getBean(eq(AirportRidesReport.class))).thenReturn(report);
    when(reportExecutionService.execute(eq(report))).thenReturn(renderer);
    when(cityService.getDefaultCity()).thenReturn(generateCity());
    when(renderer.createEmailFor(eq(report), anyList(), any(City.class))).thenReturn(email);

    testedInstance.executeInternal();

    verify(reportExecutionService, times(1)).execute(eq(report));
  }

  @Test
  public void testEmailServiceIsCalled() throws Exception {
    ReportMetadata metadata = createReportMetadata(Collections.emptySet(), REPORT_ID, AirportRidesReport.class);
    when(reportService.getReport(eq(REPORT_ID))).thenReturn(metadata);
    when(beanFactory.getBean(eq(AirportRidesReport.class))).thenReturn(report);
    when(reportExecutionService.execute(eq(report))).thenReturn(renderer);
    when(renderer.createEmailFor(eq(report), eq(RECIPIENTS_LIST), any(City.class))).thenReturn(email);
    when(email.hasAttachments()).thenReturn(true);

    testedInstance.executeInternal();

    verify(emailService, times(1)).sendEmail(eq(email));
  }

  @Test(expected = JobExecutionException.class)
  public void testJobsThrowsExceptionOnNonExistentReport() throws Exception {
    when(reportService.getReport(anyLong())).thenThrow(new NotFoundException("No report found"));

    testedInstance.executeInternal();
  }

  public City generateCity() {
    City city = new City();
    city.setAppName("RideAustin");
    city.setOffice("Office Address");
    return city;
  }

}