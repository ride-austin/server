package com.rideaustin.service.user;

import javax.annotation.Nonnull;

import org.apache.commons.mail.EmailException;

import com.google.common.collect.ImmutableMap;
import com.rideaustin.Constants;
import com.rideaustin.model.City;
import com.rideaustin.model.user.User;
import com.rideaustin.service.email.AbstractTemplateEmail;

public class UserCardIsLockedEmail extends AbstractTemplateEmail {

  public static final String TITLE = Constants.EmailTitle.USER_CARD_IS_LOCKED_EMAIL;
  private static final String TEMPLATE = "user_card_locked.ftl";

  public UserCardIsLockedEmail(@Nonnull User user, @Nonnull City city) throws EmailException {
    super(TITLE, TEMPLATE);
    setModel(ImmutableMap.of("user", user, "city", city));
    addRecipient(user.getFullName(), user.getEmail());
    setFrom(city.getContactEmail(), city.getAppName());
  }

}
