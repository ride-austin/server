package com.rideaustin.service.user;

import javax.annotation.Nonnull;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.mail.EmailException;

import com.google.common.collect.ImmutableMap;
import com.rideaustin.model.City;
import com.rideaustin.model.user.User;
import com.rideaustin.service.email.AbstractTemplateEmail;

public class PasswordResetEmail extends AbstractTemplateEmail {

  public static final String TITLE = "Password reset request confirmation";
  private static final String TEMPLATE = "reset_password.ftl";

  public PasswordResetEmail(@Nonnull User user, @Nonnull String token, String env, City city) throws EmailException {
    super(TITLE, TEMPLATE);
    final String prefix = StringUtils.isEmpty(env) ? "api" : String.format("api-%s", env);
    final String link = String.format("https://%s.example.com/password-reset?token=%s", prefix, token);
    setModel(ImmutableMap.of("user", user, "link", link, "city", city));
    addRecipient(user.getFullName(), user.getEmail());
    setFrom(city.getContactEmail(), city.getAppName());
  }

}
