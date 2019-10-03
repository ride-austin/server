package com.rideaustin.report.render;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.rideaustin.report.Report;
import com.rideaustin.report.model.ReportFormat;
import com.rideaustin.report.model.ReportMetadata;
import com.rideaustin.rest.exception.ServerError;
import com.rideaustin.service.email.CSVEmailAttachment;
import com.rideaustin.service.email.EmailAttachment;
import com.rideaustin.service.thirdparty.S3StorageService;

import au.com.bytecode.opencsv.CSVWriter;

@Component
public class CSVReportRenderer<T, P> extends BaseReportRenderer<Report<T, P>> {

  public CSVReportRenderer(S3StorageService s3StorageService) {
    super(s3StorageService);
  }

  @Override
  public Collection<EmailAttachment> doCreateAttachments(Report<T, P> report) throws ServerError {
    StringWriter writer = new StringWriter();
    try (CSVWriter csvWriter = new CSVWriter(writer)) {
      csvWriter.writeNext(report.getAdapter().getHeaders());
      report.getResultsStream().map(report.getAdapter().getRowMapper()).forEach(csvWriter::writeNext);
    } catch (IOException e) {
      throw new ServerError(e);
    }
    Optional<ReportMetadata> metadata = Optional.ofNullable(report.getMetadata());
    String name = metadata
      .map(ReportMetadata::getReportName)
      .orElseGet(() -> getDefaultReportName(report));
    return Collections.singleton(new CSVEmailAttachment(name, writer.toString()));
  }

  @Override
  public boolean canRepresent(Report report) {
    return !report.isComposite() && canRepresentFormat(report.getMetadata().getReportFormat());
  }

  @Override
  protected boolean canRepresentFormat(ReportFormat reportFormat) {
    return ReportFormat.CSV.equals(reportFormat);
  }

}
