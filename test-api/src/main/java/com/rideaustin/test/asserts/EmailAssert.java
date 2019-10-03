package com.rideaustin.test.asserts;

import static com.rideaustin.test.asserts.helpers.EmailConstants.Earnings.CUSTOM_EARNINGS_HEADER;
import static com.rideaustin.test.asserts.helpers.EmailConstants.Earnings.EARNINGS_HEADER;
import static com.rideaustin.test.asserts.helpers.EmailConstants.TripSummary.CAR_TEMPLATE;
import static com.rideaustin.test.asserts.helpers.EmailConstants.TripSummary.FARE_CREDIT;
import static com.rideaustin.test.asserts.helpers.EmailConstants.TripSummary.PRIORITY_FARE;
import static com.rideaustin.test.asserts.helpers.EmailConstants.TripSummary.RIDE_CREDIT;
import static com.rideaustin.test.asserts.helpers.EmailConstants.TripSummary.ROUND_UP;
import static com.rideaustin.test.asserts.helpers.EmailConstants.TripSummary.TIP;

import java.util.Date;
import java.util.List;

import org.assertj.core.api.AbstractAssert;

import com.rideaustin.model.enums.PaymentProvider;
import com.rideaustin.model.ride.Ride;
import com.rideaustin.service.email.InterceptingEmailService.Email;
import com.rideaustin.test.asserts.helpers.EmailSearchTerms;
import com.rideaustin.test.asserts.helpers.EmailSearchTerms.SearchTerm;
import com.rideaustin.test.asserts.helpers.EmailType;
import com.rideaustin.utils.FormatUtils;

public class EmailAssert extends AbstractAssert<EmailAssert, List<Email>> {
  public EmailAssert(List<Email> messages) {
    super(messages, EmailAssert.class);
  }

  public static EmailAssert assertThat(List<Email> messages) {
    return new EmailAssert(messages);
  }

  public EmailAssert riderSignUpEmailDelivered(Date date, String email) {
    return emailDelivered(date, email, EmailType.RIDER_SIGNUP,
      "Should have found 'Rider Sign Up' email for %s", email);
  }

  public EmailAssert riderSignUpEmailNotDelivered(Date date, String email) {
    return emailNotDelivered(date, email, EmailType.RIDER_SIGNUP,
      "Should not have found 'Rider Sign Up' email for %s", email);
  }

  public EmailAssert driverSignUpEmailDelivered(Date date, String email) {
    return emailDelivered(date, email, EmailType.DRIVER_SIGNUP,
      "Should have found 'Driver Sign Up' email for %s", email);
  }

  public EmailAssert cardIsLocked(Date date, String email) {
    return emailDelivered(date, email, EmailType.CARD_LOCKED,
      "Should have found 'Your credit card is locked' email for %s", email);
  }

  public EmailAssert paymentDeclined(Date date, String email) {
    return emailDelivered(date, email, EmailType.PAYMENT_DECLINED,
      "Should have found 'Payment was declined' email for %s", email);
  }

  public EmailAssert driverSignUpEmailNotDelivered(Date date, String email) {
    return emailNotDelivered(date, email, EmailType.DRIVER_SIGNUP,
      "Should not have found 'Driver Sign Up' email for %s", email);
  }

  public EmailAssert passwordResetDelivered(Date date, String email) {
    return emailDelivered(date, email, EmailType.PASSWORD_RESET,
      "Should have found 'Password Reset' email for %s", email);
  }

  public EmailAssert passwordResetNotDelivered(Date date, String email) {
    return emailNotDelivered(date, email, EmailType.PASSWORD_RESET,
      "Should not have found 'Password Reset' email for %s", email);
  }

  public EmailAssert userDisabledEmailDelivered(Date date, String email) {
    return emailDelivered(date, email, EmailType.USER_DISABLED,
      "Should have found 'User Disabled' email for %s", email);
  }

  public EmailAssert userDisabledEmailNotDelivered(Date date, String email) {
    return emailNotDelivered(date, email, EmailType.USER_DISABLED,
      "Should not have found 'User Disabled' email for %s", email);
  }

  public EmailAssert userEnabledEmailDelivered(Date date, String email) {
    return emailDelivered(date, email, EmailType.USER_ENABLED,
      "Should have found 'User Enabled' email for %s", email);
  }

  public EmailAssert userEnabledEmailNotDelivered(Date date, String email) {
    return emailNotDelivered(date, email, EmailType.USER_ENABLED,
      "Should not have found 'User Enabled' email for %s", email);
  }

  public EmailAssert userDeactivatedEmailDelivered(Date date, String email) {
    return emailDelivered(date, email, EmailType.USER_DEACTIVATED,
      "Should have found 'User Deactivated' email for %s", email);
  }

  public EmailAssert userDeactivatedEmailNotDelivered(Date date, String email) {
    return emailNotDelivered(date, email, EmailType.USER_DEACTIVATED,
      "Should not have found 'User Deactivated' email for %s", email);
  }

  public EmailAssert userActivatedEmailDelivered(Date date, String email) {
    return emailDelivered(date, email, EmailType.USER_ACTIVATED,
      "Should have found 'User Activated' email for %s", email);
  }

  public EmailAssert userActivatedEmailNotDelivered(Date date, String email) {
    return emailNotDelivered(date, email, EmailType.USER_ACTIVATED,
      "Should not have found 'User Activated' email for %s", email);
  }

  public EmailAssert tripSummaryEmailDeliveredWithPF(Date date, String email) {
    return emailDeliveredWithText(date, email, EmailType.RIDER_TRIP_SUMMARY, PRIORITY_FARE,
      "Should have found 'Rider Trip Summary' email with '%s' for %s", PRIORITY_FARE, email);
  }

  public EmailAssert tripSummaryEmailDeliveredWithoutPF(Date date, String email) {
    return emailDeliveredWithoutText(date, email, EmailType.RIDER_TRIP_SUMMARY, PRIORITY_FARE,
      "Should have found 'Rider Trip Summary' email without '%s' for %s", PRIORITY_FARE, email);
  }

  public EmailAssert tripSummaryEmailDeliveredWithTip(Date date, String email) {
    return emailDeliveredWithText(date, email, EmailType.RIDER_TRIP_SUMMARY, TIP,
      "Should have found 'Rider Trip Summary' email with '%s' for %s", TIP, email);
  }

  public EmailAssert tripSummaryEmailDeliveredWithDriverPhoto(Date date, String email, String photoUrl) {
    return emailDeliveredWithText(date, email, EmailType.RIDER_TRIP_SUMMARY, photoUrl,
      "Should have found 'Rider Trip Summary' email with '%s' for %s", photoUrl, email);
  }

  public EmailAssert tripSummaryEmailDeliveredWithRoundUp(Date date, String email) {
    return emailDeliveredWithText(date, email, EmailType.RIDER_TRIP_SUMMARY, ROUND_UP,
      "Should have found 'Rider Trip Summary' email with '%s' for %s", ROUND_UP, email);
  }

  public EmailAssert tripSummaryEmailDeliveredWithFareCredit(Date date, String email) {
    return emailDeliveredWithText(date, email, EmailType.RIDER_TRIP_SUMMARY, FARE_CREDIT,
      "Should have found 'Rider Trip Summary' email with '%s' for %s", FARE_CREDIT, email);
  }

  public EmailAssert tripSummaryEmailDeliveredWithRideCredit(Date date, String email) {
    return emailDeliveredWithText(date, email, EmailType.RIDER_TRIP_SUMMARY, RIDE_CREDIT,
      "Should have found 'Rider Trip Summary' email with '%s' for %s", RIDE_CREDIT, email);
  }

  public EmailAssert tripSummaryEmailDeliveredWithCarType(Date date, String email, Ride ride) {
    String distanceTravelled = FormatUtils.formatDecimal(ride.getDistanceTravelledInMiles());
    String carType = ride.getRequestedCarType().getTitle();
    final String carLabel = String.format(CAR_TEMPLATE, carType, distanceTravelled);
    return emailDeliveredWithText(date, email, EmailType.RIDER_TRIP_SUMMARY, carLabel,
      "Should have found 'Rider Trip Summary' email with '%s', for %s", carType, email);
  }

  public EmailAssert earningsEmailDelivered(Date date, String email) {
    return emailDeliveredWithText(date, email, EmailType.DRIVER_EARNINGS, EARNINGS_HEADER,
      "Should have found 'Driver Earnings' email with '%s' for %s", EARNINGS_HEADER, email);
  }

  public EmailAssert customEarningsEmailDelivered(Date date, String email) {
    return emailDeliveredWithText(date, email, EmailType.DRIVER_CUSTOM_EARNINGS, CUSTOM_EARNINGS_HEADER,
      "Should have found 'Driver Custom Earnings' email with '%s' for %s", CUSTOM_EARNINGS_HEADER, email);
  }

  public EmailAssert tripSummaryNotSent(Date start, String email) {
    isNotNull();
    SearchTerm term = EmailSearchTerms.newEmailTerm(start, email, EmailType.RIDER_TRIP_SUMMARY);
    long count = actual.stream().filter(term::match).count();
    if (count != 0) {
      failWithMessage("Expected not to receive 'Rider Trip Summary' for %s", email);
    }

    return this;
  }

  public EmailAssert tripSummaryEmailDeliveredWithApplePayIcon(Date date, String email) {
    return emailDeliveredWithText(date, email, EmailType.RIDER_TRIP_SUMMARY, PaymentProvider.APPLE_PAY.getIcon(),
      "Should have found 'Rider Trip Summary' email with ApplePay icon");
  }

  public EmailAssert cancellationDeliveredWithApplePayIcon(Date date, String email) {
    return emailDeliveredWithText(date, email, EmailType.RIDER_TRIP_CANCELLED, PaymentProvider.APPLE_PAY.getIcon(),
      "Should have found 'Rider Trip Cancellation' email with ApplePay icon");
  }

  private EmailAssert emailDelivered(Date date, String email, EmailType emailType, String errorMessage, Object... arguments) {
    isNotNull();
    SearchTerm term = EmailSearchTerms.newEmailTerm(date, email, emailType);
    long count = actual.stream().filter(term::match).count();
    if (count == 0) {
      failWithMessage(errorMessage, arguments);
    }

    return this;
  }

  private EmailAssert emailNotDelivered(Date date, String email, EmailType emailType, String errorMessage, Object... arguments) {
    isNotNull();
    SearchTerm term = EmailSearchTerms.newEmailTerm(date, email, emailType);
    long count = actual.stream().filter(term::match).count();
    if (count != 0) {
      failWithMessage(errorMessage, arguments);
    }

    return this;
  }

  private EmailAssert emailDeliveredWithText(Date date, String email, EmailType emailType, String text,
    String errorMessage, Object... arguments) {
    isNotNull();
    SearchTerm term = EmailSearchTerms.newEmailWithTextTerm(date, email, emailType, text);
    long count = actual.stream().filter(term::match).count();
    if (count != 1) {
      failWithMessage(errorMessage, arguments);
    }

    return this;
  }

  private EmailAssert emailDeliveredWithoutText(Date date, String email, EmailType emailType, String text,
    String errorMessage, Object... arguments) {
    isNotNull();
    SearchTerm term = EmailSearchTerms.newEmailWithoutTextTerm(date, email, emailType, text);
    long count = actual.stream().filter(term::match).count();
    if (count != 1) {
      failWithMessage(errorMessage, arguments);
    }

    return this;
  }
}
