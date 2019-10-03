package com.rideaustin.service.email;

import java.util.Collection;
import java.util.List;

import javax.annotation.Nullable;

import org.apache.commons.mail.EmailException;

import com.google.common.collect.ImmutableMap;
import com.rideaustin.model.City;

public class ReportEmail extends BasicEmail {

  private static final String REPORT_EMAIL_TEMPLATE = "link_report_template.ftl";
  private static final String REPORT_EMAIL_HTML_CONTENT = "Please see attached";

  public ReportEmail(EmailAttachment attachment, @Nullable String recipients, City city) throws EmailException {
    super(attachment.getName(), REPORT_EMAIL_HTML_CONTENT, recipients);
    addAttachment(attachment);
    setModel(ImmutableMap.of("city", city));
  }

  public ReportEmail(String subject, String reportLink, List<String> recipients, City city) throws EmailException {
    super(subject, REPORT_EMAIL_HTML_CONTENT, null);
    setTemplate(REPORT_EMAIL_TEMPLATE);
    addRecipients(recipients);
    setModel(ImmutableMap.of("reportLink", reportLink, "reportName", subject, "city", city));
  }

  public ReportEmail(String subject, Collection<EmailAttachment> attachments, List<String> recipients, City city)
    throws EmailException {
    super(subject, REPORT_EMAIL_HTML_CONTENT, null);
    attachments.forEach(this::addAttachment);
    addRecipients(recipients);
    setModel(ImmutableMap.of("city", city));
  }

}
