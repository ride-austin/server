package com.rideaustin.jobs;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.BeanFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rideaustin.report.Report;
import com.rideaustin.report.model.ReportFormat;
import com.rideaustin.report.model.ReportMetadata;
import com.rideaustin.report.model.ReportParameter;
import com.rideaustin.report.render.ReportRenderer;
import com.rideaustin.service.email.EmailService;
import com.rideaustin.service.email.ReportEmail;
import com.rideaustin.service.reports.ReportExecutionService;
import com.rideaustin.service.reports.ReportService;

public abstract class GenericReportJobBaseTest {

  protected static final List<String> RECIPIENTS_LIST = Collections.singletonList("test@example.com");
  protected static final String RECIPIENTS = "test@example.com";

  // real ObjectMapper instance to be used for tests (also available to subclasses)
  protected ObjectMapper objectMapper = new ObjectMapper();

  @Mock
  protected ReportService reportService;
  @Mock
  protected ReportExecutionService reportExecutionService;
  @Mock
  protected BeanFactory beanFactory;
  @Mock
  protected EmailService emailService;
  @Mock
  protected ReportEmail email;
  @Mock
  protected ReportRenderer renderer;

  @InjectMocks
  protected GenericReportJob testedInstance;

  @Before
  public void setUp() throws Exception {
    testedInstance = createTestedInstance();
    testedInstance.setObjectMapper(this.objectMapper);
    MockitoAnnotations.initMocks(this);
  }

  protected abstract GenericReportJob createTestedInstance();

  protected ReportMetadata createReportMetadata(Set<ReportParameter> parameters, long reportId, Class<? extends Report> reportClass) {
    return new ReportMetadata(reportId, "", "", ReportFormat.CSV, "", parameters, false, false, reportClass);
  }
}
