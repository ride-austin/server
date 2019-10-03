package com.rideaustin.report;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.data.domain.Page;

import com.google.common.collect.ImmutableSet;
import com.rideaustin.report.model.ReportComponent;
import com.rideaustin.report.model.ReportMetadata;
import com.rideaustin.report.model.ReportParameter;
import com.rideaustin.report.params.CampaignCompositeReportParamsProvider;
import com.rideaustin.report.params.ReportParameterProvider;
import com.rideaustin.report.render.TestReportComponent;
import com.rideaustin.rest.model.ListReportParams;
import com.rideaustin.rest.model.PagingParams;

public class ReportsMetadataProviderTest {

  @Mock
  private BeanFactory beanFactory;
  @Mock
  private ApplicationContext applicationContext;
  @Mock
  private ReportParameterProvider reportParameterProvider;

  private ReportsMetadataProvider testedInstance;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);

    testedInstance = new ReportsMetadataProvider(beanFactory);

    when(beanFactory.getBean(argThat(new BaseMatcher<Class<Object>>() {
      @Override
      public boolean matches(Object o) {
        return ImmutableSet.of(ReportComponent.NullProvider.class, CampaignCompositeReportParamsProvider.class).contains(o);
      }

      @Override
      public void describeTo(Description description) {
      }
    }))).thenReturn(new ReportComponent.NullProvider());
    testedInstance.onApplicationEvent(new ContextRefreshedEvent(applicationContext));
  }

  @Test
  public void onLoadLoadsReportMetadata() {
    final Optional<ReportMetadata> metadata = testedInstance.findByClass(TestReportComponent.class);

    assertTrue(metadata.isPresent());
  }

  @Test
  public void findReportsFiltersReportsByName() {
    final String reportName = "Test report";
    final ListReportParams searchCriteria = new ListReportParams();
    searchCriteria.setReportName(reportName);

    final Page<ReportMetadata> result = testedInstance.findReports(searchCriteria, new PagingParams());

    assertEquals(1, result.getTotalElements());
    assertEquals(reportName, result.getContent().get(0).getReportName());
  }

  @Test
  public void listParametersCollectsParameters() {
    final List<ReportParameter> result = testedInstance.listParameters(1000);

    assertEquals(2, result.size());
    assertEquals(1, result.get(0).getOrder());
    assertEquals("Param 1", result.get(0).getParameterLabel());
    assertEquals(3, result.get(1).getOrder());
    assertEquals("Param 3", result.get(1).getParameterLabel());
  }
}