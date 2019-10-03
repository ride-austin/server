package com.rideaustin.report.render;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.rideaustin.Constants;
import com.rideaustin.model.City;
import com.rideaustin.report.Report;
import com.rideaustin.report.adapter.ReportAdapter;
import com.rideaustin.report.model.ReportFormat;
import com.rideaustin.report.model.ReportMetadata;
import com.rideaustin.report.model.ReportParameter;
import com.rideaustin.report.model.ReportParameterType;
import com.rideaustin.rest.exception.RideAustinException;
import com.rideaustin.service.CityService;
import com.rideaustin.service.email.AbstractTemplateEmail;
import com.rideaustin.service.email.EmailAttachment;
import com.rideaustin.service.email.ReportEmail;
import com.rideaustin.service.thirdparty.S3StorageService;
import com.rideaustin.service.thirdparty.StorageItem;
import org.apache.commons.mail.EmailException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class CSVReportRendererTest {

  private static final List<String> RECIPIENT = Collections.singletonList("test@example.com");
  private static final Instant NOW = Instant.now();
  private static final String CSV_CONTENT = "\"header 1\",\"header 2\"".concat("\n") + "\"a\",\"1\"".concat("\n");

  private CSVReportRenderer testedInstance;

  @Mock
  private Report report;
  @Mock
  private ReportMetadata reportMetadata;
  @Mock
  private S3StorageService s3StorageService;
  @Mock
  private CityService cityService;

  @Before
  public void setUp() throws Exception {
    ReportAdapter<Map.Entry<String, Integer>> adapter = new ReportAdapter<Map.Entry<String, Integer>>() {
      @Override
      public Function<Map.Entry<String, Integer>, String[]> getRowMapper() {
        return e -> new String[]{e.getKey(), String.valueOf(e.getValue())};
      }

      @Override
      public Map<String, Object> getReportContext() {
        return ImmutableMap.of(
          "var", "value",
          "date", NOW
        );
      }

      @Override
      public String[] getHeaders() {
        return new String[]{
          "header 1",
          "header 2"
        };
      }
    };
    MockitoAnnotations.initMocks(this);
    testedInstance = new CSVReportRenderer(s3StorageService);
    when(report.getAdapter()).thenReturn(adapter);
    when(report.getMetadata()).thenReturn(reportMetadata);
    when(report.getResultsStream()).thenReturn(ImmutableMap.of("a", 1).entrySet().stream());
    when(cityService.getDefaultCity()).thenReturn(generateCity());
  }

  @Test
  public void testGetRawContentWritesCSV() throws Exception {

    EmailAttachment attachment = ((Collection<EmailAttachment>) testedInstance.createAttachments(report)).iterator().next();
    String rawContent = new String(attachment.getBytes());

    assertEquals(CSV_CONTENT, rawContent);
  }

  @Test
  public void testCreateEmailFillsContextVariablesInReportHeader() throws Exception {
    ReportParameter contextParameter = new ReportParameter("", "var", "", ReportParameterType.STRING, false, false, "", 0, null);
    when(report.getMetadata()).thenReturn(createMetadata("Report with {var}", contextParameter, false, ""));

    AbstractTemplateEmail email = testedInstance.createEmailFor(report, RECIPIENT, generateCity());

    assertEquals("Report with value", email.getSubject());
  }

  @Test
  public void testCreateEmailFillsDateContextVariablesInReportHeader() throws RideAustinException, EmailException {
    ReportParameter contextParameter = new ReportParameter("", "date", "", ReportParameterType.DATE, false, false, "", 0, null);
    when(report.getMetadata()).thenReturn(createMetadata("Report for {date}", contextParameter, false, ""));

    AbstractTemplateEmail email = testedInstance.createEmailFor(report, RECIPIENT, generateCity());

    assertEquals(String.format("Report for %s", Constants.DATE_FORMATTER.format(NOW)), email.getSubject());
  }

  @Test
  public void testCreateEmailFillsDateTimeContextVariablesInReportHeader() throws RideAustinException, EmailException {
    ReportParameter contextParameter = new ReportParameter("", "date", "", ReportParameterType.DATETIME, false, false, "", 0, null);
    when(report.getMetadata()).thenReturn(createMetadata("Report for {date}", contextParameter, false, ""));

    AbstractTemplateEmail email = testedInstance.createEmailFor(report, RECIPIENT, generateCity());

    assertEquals(String.format("Report for %s", Constants.DATETIME_FORMATTER.format(NOW)), email.getSubject());
  }

  @Test
  public void testCreateEmailCreatesCSVAttachments() throws RideAustinException, EmailException {
    ReportParameter contextParameter = new ReportParameter("", "name", "", ReportParameterType.STRING, false, false, "", 0, null);
    String reportHeader = "Report";
    when(this.report.getMetadata()).thenReturn(createMetadata(reportHeader, contextParameter, false, ""));

    AbstractTemplateEmail email = testedInstance.createEmailFor(this.report, RECIPIENT, generateCity());

    assertTrue(email instanceof ReportEmail);
    assertEquals(reportHeader, email.getSubject());
  }

  @Test
  public void createEmailForUploadsReport() throws RideAustinException, EmailException {
    ReportParameter contextParameter = new ReportParameter("", "name", "", ReportParameterType.STRING, false, false, "", 0, null);
    String reportHeader = "Report";
    when(this.report.getMetadata()).thenReturn(createMetadata(reportHeader, contextParameter, true, "TestReport"));
    when(s3StorageService.getSignedURL(anyString())).thenReturn("123");
    when(s3StorageService.uploadStorageItem(any(StorageItem.class))).thenReturn("1665");

    AbstractTemplateEmail email = testedInstance.createEmailFor(this.report, RECIPIENT, generateCity());

    assertTrue(email instanceof ReportEmail);
    verify(s3StorageService, times(1)).uploadStorageItem(any(StorageItem.class));
  }

  @Test
  public void testCanRepresentSupportsCSV() {
    when(report.isComposite()).thenReturn(false);
    when(report.getMetadata()).thenReturn(reportMetadata);
    when(reportMetadata.getReportFormat()).thenReturn(ReportFormat.CSV);
    assertTrue(testedInstance.canRepresent(report));
  }

  @Test
  public void testCanRepresentSupportsOnlyCSV() {
    when(report.isComposite()).thenReturn(false);
    when(report.getMetadata()).thenReturn(reportMetadata);
    for (ReportFormat reportFormat : ReportFormat.values()) {
      when(reportMetadata.getReportFormat()).thenReturn(reportFormat);
      if (testedInstance.canRepresent(report) && !ReportFormat.CSV.equals(reportFormat)) {
        fail("This renderer must support only CSV");
      }
    }
  }

  private ReportMetadata createMetadata(String reportHeader, ReportParameter contextParameter, final boolean upload, final String reportName) {
    return new ReportMetadata(1L, reportName, "", ReportFormat.CSV, reportHeader,
      ImmutableSet.of(contextParameter), false, upload, null);
  }

  public City generateCity() {
    City city = new City();
    city.setAppName("RideAustin");
    city.setOffice("Office Address");
    return city;
  }
}