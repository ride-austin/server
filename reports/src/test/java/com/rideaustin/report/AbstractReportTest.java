package com.rideaustin.report;

import org.junit.Before;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.core.env.Environment;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.rideaustin.service.reports.ReportParametersDefaultValueProvider;
import com.rideaustin.service.reports.ReportParametersValidator;

public abstract class AbstractReportTest<R extends BaseReport> {

  @Mock
  protected Environment environment;
  @Mock
  protected ReportParametersValidator parametersValidator;
  @Mock
  protected ReportParametersDefaultValueProvider defaultValuesProvider;

  protected R testedInstance;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    testedInstance = doCreateTestedInstance();
    ObjectMapper mapper = new ObjectMapper();
    mapper.registerModule(new JavaTimeModule());
    testedInstance.setMapper(mapper);
    testedInstance.setDefaultValuesProvider(defaultValuesProvider);
    testedInstance.setParametersValidator(parametersValidator);
  }

  protected abstract R doCreateTestedInstance();
}
