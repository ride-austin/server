package com.rideaustin.service.email.monitor;

import java.util.List;

import com.rideaustin.service.email.EmailService;
import com.rideaustin.service.email.InterceptingEmailService;
import com.rideaustin.service.email.InterceptingEmailService.Email;

public class SimpleEmailCheckerService implements EmailCheckerService {

  private static final int DEFAULT_START = 1;

  private final InterceptingEmailService emailService;

  public SimpleEmailCheckerService(EmailService emailService) {
    this.emailService = (InterceptingEmailService) emailService;
  }

  @Override
  public List<Email> fetchEmails(int end) {
    return fetchEmails(DEFAULT_START, end);
  }

  @Override
  public List<Email> fetchEmails(int start, int end) {
    return fetch(start, end);
  }

  @Override
  public void close() {
    emailService.reset();
  }

  private List<Email> fetch(int start, int end) {
    return emailService.getMessages().subList(start-1, Math.min(end-1, emailService.getMessages().size()));
  }

}
