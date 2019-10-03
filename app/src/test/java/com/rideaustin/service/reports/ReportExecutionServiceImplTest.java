package com.rideaustin.service.reports;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.rideaustin.report.Report;
import com.rideaustin.report.render.ReportRenderer;
import com.rideaustin.report.render.ReportRendererFactory;
import com.rideaustin.rest.exception.RideAustinException;

public class ReportExecutionServiceImplTest {

  @Mock
  private ReportRendererFactory representationFactory;

  @Mock
  private Report report;

  @Mock
  private ReportRenderer mockRenderer;

  private ReportExecutionServiceImpl testedInstance;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
    testedInstance = new ReportExecutionServiceImpl(representationFactory);
  }

  @Test
  public void testExecuteExecutesReport() throws Exception {
    testedInstance.execute(report);

    verify(report, times(1)).execute();
  }

  @Test
  public void testExecuteReturnsRenderer() throws RideAustinException {
    when(representationFactory.getRendererFor(eq(report))).thenReturn(mockRenderer);

    ReportRenderer renderer = testedInstance.execute(report);

    verify(representationFactory, times(1)).getRendererFor(eq(report));
    assertEquals(mockRenderer, renderer);
  }

}