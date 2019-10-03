package com.rideaustin.report.render;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.rideaustin.report.Report;
import com.rideaustin.report.render.BaseCompositeReportRendererTest.TestReport;
import com.rideaustin.rest.exception.RideAustinException;
import com.rideaustin.rest.exception.ServerError;

public class ReportRendererFactoryTest {

  @Mock
  private ReportRenderer reportRenderer;

  private ReportRendererFactory testedInstance;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);

    testedInstance = new ReportRendererFactory(Collections.singleton(reportRenderer));
  }

  @Test
  public void getRendererForReturnsSupportingRenderer() throws RideAustinException {
    when(reportRenderer.canRepresent(any(Report.class))).thenReturn(true);

    final ReportRenderer result = testedInstance.getRendererFor(new TestReport());

    assertEquals(reportRenderer, result);
  }

  @Test(expected = ServerError.class)
  public void getRendererForThrowsExceptionWhenNoSupportingRendererFound() throws RideAustinException {
    final ReportRenderer result = testedInstance.getRendererFor(new TestReport());
  }
}