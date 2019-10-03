package com.rideaustin.service.thirdparty;

import java.io.IOException;
import java.io.StringWriter;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

public abstract class AbstractTemplateSMS {

  private final String template;

  private Map<String, Object> model = Maps.newHashMap();
  private final List<String> recipients = Lists.newArrayList();

  public AbstractTemplateSMS(String template) {
    this.template = template;
  }

  protected void setModel(Map<String, Object> model){
    this.model = model;
  }

  public Map<String, Object> getModel()  {
    return this.model;
  }

  protected void addRecipient(String phoneNumber) {
    recipients.add(phoneNumber);
  }

  protected List<String> getRecipients() {
    return recipients;
  }

  protected String processTemplate(Configuration configuration) throws SMSException {
    if (template == null) {
      throw new SMSException("Unknown template");
    }
    try {
      StringWriter sw = new StringWriter();
      Template freemarkerTemplate = configuration.getTemplate(this.template);
      freemarkerTemplate.process(model, sw);
      return sw.toString();
    } catch (IOException | TemplateException e) {
      throw new SMSException(e);
    }
  }

}
