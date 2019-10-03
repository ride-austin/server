package com.rideaustin.service.email;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.mail.EmailException;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import com.rideaustin.repo.dsl.UserDslRepository;

import freemarker.template.Configuration;

@Service
@Profile("itest")
public class InterceptingEmailService extends EmailService {

  private List<Email> messages;
  private boolean debug = false;

  public InterceptingEmailService(Environment env, Configuration configuration, UserDslRepository userDslRepository) {
    super(env, configuration, userDslRepository);
    this.messages = new ArrayList<>();
  }

  @Override
  protected void doSendEmail(AbstractTemplateEmail email) throws EmailException {
    this.messages.add(new Email(email.getSubject(), new HashSet<>(email.getRecipientList()), email.getHtmlMessage(), email.getFromAddress().getAddress()));
    if (debug) {
      super.doSendEmail(email);
    }
  }

  public List<Email> getMessages() {
    return Collections.unmodifiableList(messages);
  }

  public void setDebug(boolean debug) {
    this.debug = debug;
  }

  public void reset() {
    this.messages.clear();
    this.debug = false;
  }

  public static class Email {
    private final String subject;
    private final Set<String> recipients;
    private final String body;
    private final String sender;
    private final Date date;

    public Email(String subject, Set<String> recipients, String body, String sender) {
      this.subject = subject;
      this.recipients = recipients;
      this.body = body;
      this.sender = sender;
      this.date = new Date();
    }

    public String getSubject() {
      return subject;
    }

    public Set<String> getRecipients() {
      return recipients;
    }

    public String getBody() {
      return body;
    }

    public String getSender() {
      return sender;
    }

    public Date getDate() {
      return date;
    }
  }
}
