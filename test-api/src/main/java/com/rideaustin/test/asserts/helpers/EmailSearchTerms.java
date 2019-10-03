package com.rideaustin.test.asserts.helpers;

import static com.rideaustin.test.asserts.helpers.EmailConstants.Earnings.CUSTOM_EARNINGS_TITLE_PREFIX;
import static com.rideaustin.test.asserts.helpers.EmailConstants.Earnings.EARNINGS_TITLE_PREFIX;

import java.util.Date;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.ImmutableMap;
import com.rideaustin.Constants;
import com.rideaustin.service.email.InterceptingEmailService.Email;

public class EmailSearchTerms {

  private static final Map<EmailType, String> EMAIL_TITLE_MAP;

  static {
    EMAIL_TITLE_MAP = ImmutableMap.<EmailType, String>builder()
      .put(EmailType.RIDER_SIGNUP, Constants.EmailTitle.RIDER_SIGNUP_EMAIL)
      .put(EmailType.PASSWORD_RESET, Constants.EmailTitle.PASSWORD_RESET_EMAIL)
      .put(EmailType.DRIVER_SIGNUP, Constants.EmailTitle.DRIVER_SIGNUP_EMAIL)
      .put(EmailType.USER_ACTIVATED, Constants.EmailTitle.USER_IS_ACTIVATED_EMAIL)
      .put(EmailType.USER_DEACTIVATED, Constants.EmailTitle.USER_IS_DEACTIVATED_EMAIL)
      .put(EmailType.USER_ENABLED, Constants.EmailTitle.USER_IS_ENABLED_EMAIL)
      .put(EmailType.USER_DISABLED, Constants.EmailTitle.USER_IS_DISABLED_EMAIL)
      .put(EmailType.RIDER_TRIP_SUMMARY, Constants.EmailTitle.END_RIDE_EMAIL)
      .put(EmailType.RIDER_TRIP_CANCELLED, Constants.EmailTitle.RIDE_CANCELLATION_EMAIL)
      .put(EmailType.CARD_LOCKED, Constants.EmailTitle.USER_CARD_IS_LOCKED_EMAIL)
      .put(EmailType.PAYMENT_DECLINED, Constants.EmailTitle.INVALID_PAYMENT_EMAIL)
      .put(EmailType.DRIVER_EARNINGS, EARNINGS_TITLE_PREFIX)
      .put(EmailType.DRIVER_CUSTOM_EARNINGS, CUSTOM_EARNINGS_TITLE_PREFIX)
      .build();
  }

  public static EmailTerm newEmailTerm(Date date, String recipient, EmailType emailType) {
    String subject = Optional.ofNullable(EMAIL_TITLE_MAP.get(emailType)).orElse(StringUtils.EMPTY);
    return new EmailTerm(date, recipient, subject);
  }

  public static EmailWithTextTerm newEmailWithTextTerm(Date date, String recipient, EmailType emailType, String text) {
    String subject = Optional.ofNullable(EMAIL_TITLE_MAP.get(emailType)).orElse(StringUtils.EMPTY);
    return new EmailWithTextTerm(date, recipient, subject, text);
  }

  public static EmailWithoutTextTerm newEmailWithoutTextTerm(Date date, String recipient, EmailType emailType, String text) {
    String subject = Optional.ofNullable(EMAIL_TITLE_MAP.get(emailType)).orElse(StringUtils.EMPTY);
    return new EmailWithoutTextTerm(date, recipient, subject, text);
  }

  public interface SearchTerm {
    boolean match(Email message);
  }

  public static class EmailTerm implements SearchTerm {
    private final Date dateTerm;

    private final String recipientTerm;

    private final String subjectTerm;

    public EmailTerm(Date date, String recipient, String subject) {
      this.dateTerm = date;
      this.recipientTerm = recipient;
      this.subjectTerm = subject;
    }

    @Override
    public boolean match(Email message) {
      return dateTerm.before(message.getDate()) && message.getRecipients().contains(recipientTerm) && message.getSubject().contains(subjectTerm);
    }
  }

  public static class EmailWithTextTerm implements SearchTerm {
    private final EmailTerm emailTerm;

    private final String bodyTerm;

    public EmailWithTextTerm(Date date, String recipient, String subject, String text) {
      this.emailTerm = new EmailTerm(date, recipient, subject);
      this.bodyTerm = text;
    }

    @Override
    public boolean match(Email message) {
      return emailTerm.match(message) && message.getBody().contains(bodyTerm);
    }
  }

  public static class EmailWithoutTextTerm implements SearchTerm {
    private final EmailTerm emailTerm;

    private final String bodyTerm;

    public EmailWithoutTextTerm(Date date, String recipient, String subject, String text) {
      this.emailTerm = new EmailTerm(date, recipient, subject);
      this.bodyTerm = text;
    }

    @Override
    public boolean match(Email message) {
      return emailTerm.match(message) && !message.getBody().contains(bodyTerm);
    }
  }
}
