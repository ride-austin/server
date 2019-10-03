package com.rideaustin.service.reports;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.internal.matchers.StartsWith;

import com.google.common.collect.ImmutableMap;
import com.rideaustin.jobs.GenericReportJob;
import com.rideaustin.model.user.User;
import com.rideaustin.report.ReportsMetadataProvider;
import com.rideaustin.report.model.ReportParameter;
import com.rideaustin.report.model.ReportParameterType;
import com.rideaustin.rest.exception.NotFoundException;
import com.rideaustin.rest.model.ListReportParams;
import com.rideaustin.rest.model.PagingParams;
import com.rideaustin.service.CurrentUserService;
import com.rideaustin.service.SchedulerService;

public class ReportServiceTest {

  private static final long REPORT_ID = 1L;

  private ReportService testedInstance;

  @Mock
  private ReportsMetadataProvider metadataProvider;

  @Mock
  private SchedulerService schedulerService;

  @Mock
  private CurrentUserService currentUserService;

  @Mock
  private User user;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
    testedInstance = new ReportService(schedulerService, currentUserService, metadataProvider);
  }

  @Test
  public void listReports() throws Exception {
    ListReportParams searchCriteria = new ListReportParams();
    PagingParams paging = new PagingParams();

    testedInstance.listReports(searchCriteria, paging);

  }

  @Test
  public void getReport() throws Exception {

  }

  @Test(expected = NotFoundException.class)
  public void getReportThrowsNotFoundException() throws NotFoundException {
    when(metadataProvider.findOne(eq(REPORT_ID))).thenReturn(Optional.empty());
    testedInstance.getReport(REPORT_ID);
  }

  @Test
  public void listParameters() throws Exception {
    testedInstance.listParameters(REPORT_ID);

  }

  @Test
  public void testListParametersSortsParametersByOrderAsc() {
    ReportParameter param1 = new ReportParameter("", "", "", ReportParameterType.STRING, false, false, null, 2, null);
    ReportParameter param2 = new ReportParameter("", "", "", ReportParameterType.STRING, false, false, null, 1, null);

    when(metadataProvider.listParameters(eq(REPORT_ID))).thenReturn(Arrays.asList(param2, param1));
    List<ReportParameter> reportParameters = testedInstance.listParameters(REPORT_ID);

    assertEquals(1, reportParameters.get(0).getOrder());
    assertEquals(2, reportParameters.get(1).getOrder());
  }

  @Test
  public void executeReport() throws Exception {
    String email = "test@example.com";
    String paramsJson = "{\"a\":10}";

    when(currentUserService.getUser()).thenReturn(user);
    when(user.getEmail()).thenReturn(email);

    testedInstance.executeReport(REPORT_ID, paramsJson);

    verify(schedulerService, times(1)).triggerJob(eq(GenericReportJob.class),
      argThat(new StartsWith(String.format("report_%d", REPORT_ID))),
      eq("reports"),
      eq(ImmutableMap.of(
        "reportId", REPORT_ID,
        "paramsJson", paramsJson,
        "recipients", email
      ))
    );
  }

}