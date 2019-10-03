package com.rideaustin.service.email;

import javax.inject.Inject;

import org.apache.commons.mail.EmailConstants;
import org.apache.commons.mail.EmailException;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import com.rideaustin.model.user.User;
import com.rideaustin.repo.dsl.UserDslRepository;

import freemarker.template.Configuration;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Profile("!itest")
public class EmailService {

  private String username;
  private String password;
  private String host;
  private Integer port;
  private Boolean debug;
  private String fromAddress;
  private String fromName;
  private String bcc;

  private final Configuration configuration;
  private final UserDslRepository userDslRepository;

  @Inject
  public EmailService(Environment env, Configuration configuration, UserDslRepository userDslRepository) {
    username = env.getProperty("smtp.username");
    password = env.getProperty("smtp.password");
    host = env.getProperty("smtp.host");
    port = env.getProperty("smtp.port", Integer.class);
    debug = env.getProperty("smtp.debug", Boolean.class);
    fromAddress = env.getProperty("email.sender.address");
    fromName = env.getProperty("email.sender.name");
    bcc = env.getProperty("email.bcc");
    this.configuration = configuration;
    this.userDslRepository = userDslRepository;
  }

  public void sendEmail(AbstractTemplateEmail email) throws EmailException {
    for (String emailAddress : email.getRecipientList()) {
      User user = userDslRepository.findAnyByEmail(emailAddress);
      if (user != null && !user.isEmailVerified()) {
        log.info("Email address {} is not verified, emails won't be sent", emailAddress);
        return;
      }
    }
    log.debug("Sending email to: {}", email.getToAddresses());
    email.setAuthentication(username, password);
    email.setHostName(host);
    email.setSmtpPort(port);
    email.getMailSession().getProperties().setProperty("mail.smtp.localhost", host);
    email.setDebug(debug);
    email.setCharset(EmailConstants.UTF_8);
    if (email.getFromAddress() == null) {
      email.setFrom(fromAddress, fromName);
    }
    email.processTemplate(configuration);
    //global mailtrap bcc for analysis purposes
    if (bcc != null) {
      email.addBcc(bcc.split(","));
    }
    doSendEmail(email);
  }

  protected void doSendEmail(AbstractTemplateEmail email) throws EmailException {
    email.send();
  }
}
