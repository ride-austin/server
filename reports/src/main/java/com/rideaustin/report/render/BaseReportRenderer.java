package com.rideaustin.report.render;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.mail.EmailException;
import org.zeroturnaround.zip.ByteSource;
import org.zeroturnaround.zip.ZipEntrySource;
import org.zeroturnaround.zip.ZipUtil;

import com.rideaustin.model.City;
import com.rideaustin.report.FormatAs;
import com.rideaustin.report.Report;
import com.rideaustin.report.model.ReportFormat;
import com.rideaustin.report.model.ReportParameter;
import com.rideaustin.report.model.ReportParameterType;
import com.rideaustin.rest.exception.RideAustinException;
import com.rideaustin.rest.exception.ServerError;
import com.rideaustin.service.email.EmailAttachment;
import com.rideaustin.service.email.ReportEmail;
import com.rideaustin.service.email.ZipEmailAttachment;
import com.rideaustin.service.thirdparty.S3StorageService;
import com.rideaustin.service.thirdparty.StorageItem;

public abstract class BaseReportRenderer<S extends Report<?, ?>> implements ReportRenderer<S> {

  private static final int EXPIRATION_HOURS = 48;

  private final S3StorageService s3StorageService;

  public BaseReportRenderer(S3StorageService s3StorageService) {
    this.s3StorageService = s3StorageService;
  }

  @Override
  public ReportEmail createEmailFor(S report, List<String> recipients, City city) throws RideAustinException {
    String reportHeader = fillContextVariables(report);
    Collection<EmailAttachment> attachments = createAttachments(report);

    try {
      if (report.getMetadata().getUpload()) {
        if (attachments.size() > 1) {
          throw new ServerError("Multiple attachments uploads not supported");
        }
        EmailAttachment attachment = attachments.iterator().next();
        StorageItem item = buildStorageItem(attachment);
        String key = s3StorageService.uploadStorageItem(item);
        String url = s3StorageService.getSignedURL(key);
        return new ReportEmail(reportHeader, url, recipients, city);
      } else {
        return new ReportEmail(reportHeader, attachments, recipients, city);
      }
    } catch (EmailException e) {
      throw new ServerError(e);
    }
  }

  protected Collection<EmailAttachment> createAttachments(S report) throws ServerError {
    Collection<EmailAttachment> attachments = doCreateAttachments(report);
    if (shouldArchiveAttachment(report)) {
      return Collections.singleton(archiveAttachments(report.getMetadata().getReportName(), attachments));
    }
    return attachments;
  }

  private boolean shouldArchiveAttachment(S report) {
    return report.getMetadata().getArchive() || report.getMetadata().getUpload();
  }

  private EmailAttachment archiveAttachments(String archiveName, Collection<EmailAttachment> attachments)
    throws ServerError {
    try {
      ZipEntrySource[] entries = attachments
        .stream()
        .map(a -> new ByteSource(a.getName(), a.getBytes()))
        .toArray(ZipEntrySource[]::new);
      File tempFile = File.createTempFile(archiveName, String.valueOf(Instant.now().toEpochMilli()));
      ZipUtil.pack(entries, tempFile);
      return new ZipEmailAttachment(archiveName, tempFile);
    } catch (IOException e) {
      throw new ServerError(e);
    }
  }

  protected abstract Collection<EmailAttachment> doCreateAttachments(S report) throws ServerError;

  protected abstract boolean canRepresentFormat(ReportFormat reportFormat);

  private String fillContextVariables(S report) {
    String reportHeader = report.getMetadata().getReportHeader();
    for (Map.Entry<String, Object> contextEntry : report.getAdapter().getReportContext().entrySet()) {
      Object value = contextEntry.getValue();
      String contextKey = contextEntry.getKey();
      Optional<ReportParameterType> paramTypeOpt = report.getMetadata().getParameters()
        .stream()
        .filter(p -> p.getParameterName().equals(contextKey))
        .map(ReportParameter::getParameterType)
        .findFirst();
      String formattedValue;
      if (!paramTypeOpt.isPresent()) {
        formattedValue = FormatAs.formatFor(value.getClass()).toString(value);
      } else {
        ReportParameterType parameterType = paramTypeOpt.get();
        formattedValue = parameterType.applyFormat(value);
      }
      reportHeader = reportHeader.replaceAll(String.format("\\{%s\\}", contextKey), formattedValue);
    }
    return reportHeader;
  }

  protected String getDefaultReportName(Report report) {
    return StringUtils.join(StringUtils.splitByCharacterTypeCamelCase(report.getClass().getSimpleName()), " ");
  }

  private StorageItem buildStorageItem(EmailAttachment attachment) {
    return StorageItem.builder()
      .setContent(attachment.getBytes())
      .setMimeType(attachment.getContentType().toString())
      .setExpirationHours(EXPIRATION_HOURS)
      .build();
  }

}
