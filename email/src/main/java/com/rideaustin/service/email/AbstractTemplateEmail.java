package com.rideaustin.service.email;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.activation.DataSource;
import javax.annotation.Nullable;
import javax.mail.util.ByteArrayDataSource;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

public abstract class AbstractTemplateEmail extends HtmlEmail {

  private String template;

  private Map<String, Object> model = Maps.newHashMap();
  private final List<Pair<String, String>> recipients = Lists.newArrayList();
  private final List<Pair<String, DataSource>> emailAttachments = Lists.newArrayList();

  public AbstractTemplateEmail(String subject, String template) {
    this.setSubject(subject);
    this.template = template;
  }

  @Override
  public String send() throws EmailException {
    processAttachments();
    processRecipients();
    return super.send();
  }

  protected void setModel(Map<String, Object> model) {
    this.model = model;
  }

  public Map<String, Object> getModel() {
    return this.model;
  }

  public List<String> getRecipientList() {
    return recipients.stream().map(Pair::getRight).collect(Collectors.toList());
  }

  public boolean hasAttachments() {
    return !emailAttachments.isEmpty();
  }

  protected void addRecipient(String email) {
    recipients.add(Pair.of(null, email));
  }

  protected void addRecipient(String fullName, String email) {
    recipients.add(Pair.of(fullName, email));
  }

  public void addRecipients(@Nullable String recipients) {
    if (recipients != null) {
      addRecipients(Stream.of(recipients.split(",")));
    }
  }

  protected void addRecipients(@Nullable Collection<String> recipients) {
    if (recipients != null) {
      addRecipients(recipients.stream());
    }
  }

  private void addRecipients(Stream<String> stream) {
    stream.map(String::trim).forEach(this::addRecipient);
  }

  void addAttachment(EmailAttachment attachment) {
    emailAttachments.add(Pair.of(attachment.getName(), new ByteArrayDataSource(attachment.getBytes(), attachment.getContentType().toString())));
  }

  private void processAttachments() throws EmailException {
    for (Pair<String, DataSource> attachment : emailAttachments) {
      this.embed(attachment.getRight(), attachment.getLeft());
    }
  }

  private void processRecipients() throws EmailException {
    for (Pair<String, String> recipient : recipients) {
      this.addTo(recipient.getRight(), recipient.getLeft());
    }
  }

  void processTemplate(Configuration configuration) throws EmailException {
    if (template == null) {
      return;
    }
    try {
      StringWriter sw = new StringWriter();
      Template freemarkerTemplate = configuration.getTemplate(this.template);
      freemarkerTemplate.process(model, sw);
      this.setHtmlMsg(sw.toString());
    } catch (IOException | TemplateException e) {
      throw new EmailException(e);
    }
  }

  public void setTemplate(String template) {
    this.template = template;
  }

  /**
   * Returning html for internal testing purposes
   * @return
   */
  String getHtmlMessage() {
    return html;
  }


}
