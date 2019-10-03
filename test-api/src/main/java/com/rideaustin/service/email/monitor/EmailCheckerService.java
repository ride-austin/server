package com.rideaustin.service.email.monitor;

import java.util.List;

import com.rideaustin.service.email.InterceptingEmailService.Email;

public interface EmailCheckerService {
  List<Email> fetchEmails(int end);

  List<Email> fetchEmails(int start, int end);

  void close();
}
