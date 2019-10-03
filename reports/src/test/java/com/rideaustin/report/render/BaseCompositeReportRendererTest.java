package com.rideaustin.report.render;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.rideaustin.report.BaseCompositeReport;
import com.rideaustin.report.BaseReport;
import com.rideaustin.report.Report;
import com.rideaustin.report.adapter.DefaultReportAdapter;
import com.rideaustin.report.adapter.ReportAdapter;
import com.rideaustin.report.entry.CompositeReportEntry;
import com.rideaustin.report.model.ReportFormat;
import com.rideaustin.report.model.ReportMetadata;
import com.rideaustin.report.params.BaseStartEndDateParams;
import com.rideaustin.rest.exception.ServerError;
import com.rideaustin.service.email.CSVEmailAttachment;
import com.rideaustin.service.email.EmailAttachment;
import com.rideaustin.service.thirdparty.S3StorageService;

public class BaseCompositeReportRendererTest {

  @Mock
  private BaseReportRenderer renderer;
  @Mock
  private S3StorageService s3StorageService;

  private BaseCompositeReportRenderer<TestCompositeReportParams> testedInstance;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);

    testedInstance = new BaseCompositeReportRenderer<>(renderer, s3StorageService);
  }

  @Test
  public void doCreateAttachmentsAddsAllReports() throws ServerError {
    final Set<CSVEmailAttachment> attachments = Collections.singleton(new CSVEmailAttachment("A", "B"));
    when(renderer.doCreateAttachments(any(Report.class))).thenReturn(attachments);

    final Collection<EmailAttachment> result = testedInstance.doCreateAttachments(new TestCompositeReport(new TestReport()));

    assertTrue(CollectionUtils.isEqualCollection(attachments, result));
  }

  @Test
  public void canRepresentReturnsTrueOnlyForCompositeReports() {
    final TestCompositeReport report = new TestCompositeReport(new TestReport());
    report.setMetadata(new ReportMetadata(1L, "A", "B", ReportFormat.CSV, "D",
      Collections.emptySet(), false, false, TestCompositeReport.class));
    when(renderer.canRepresentFormat(ReportFormat.CSV)).thenReturn(true);

    final boolean result = testedInstance.canRepresent(report);

    assertTrue(result);
  }

  static class TestCompositeReportParams extends BaseStartEndDateParams {
  }

  static class TestEntry {

  }

  static class TestReport extends BaseReport<TestEntry, TestCompositeReportParams> {

    @Override
    protected ReportAdapter<TestEntry> createAdapter() {
      return null;
    }

    @Override
    protected void doExecute() {

    }
  }

  static class TestCompositeReport extends BaseCompositeReport<TestCompositeReportParams> {

    public TestCompositeReport(TestReport testReport) {
      super(testReport);
    }

    @Override
    protected ReportAdapter<CompositeReportEntry> createAdapter() {
      return new DefaultReportAdapter<>(CompositeReportEntry.class, Collections.emptyMap());
    }
  }
}