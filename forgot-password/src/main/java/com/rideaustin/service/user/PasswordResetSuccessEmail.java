package com.rideaustin.service.user;

import javax.annotation.Nonnull;

import org.apache.commons.mail.EmailException;

import com.google.common.collect.ImmutableMap;
import com.rideaustin.Constants;
import com.rideaustin.model.City;
import com.rideaustin.model.user.User;
import com.rideaustin.service.email.AbstractTemplateEmail;

public class PasswordResetSuccessEmail extends AbstractTemplateEmail {

  public static final String TITLE = Constants.EmailTitle.PASSWORD_RESET_EMAIL;
  private static final String TEMPLATE = "reset_password_success.ftl";

  public PasswordResetSuccessEmail(@Nonnull User user, @Nonnull String password, City city) throws EmailException {
    super(TITLE, TEMPLATE);
    setModel(ImmutableMap.of("user", user, "password", password, "city", city));
    addRecipient(user.getFullName(), user.getEmail());
    setFrom(city.getContactEmail(), city.getAppName());
  }
}
