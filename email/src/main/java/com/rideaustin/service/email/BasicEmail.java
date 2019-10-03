package com.rideaustin.service.email;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.mail.EmailException;

public class BasicEmail extends AbstractTemplateEmail {

  public BasicEmail(@Nonnull String subject, @Nonnull String content, @Nullable String recipients) throws EmailException {
    super(subject, null);
    setHtmlMsg(content);
    addRecipients(recipients);
  }

}
