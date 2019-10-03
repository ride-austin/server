package com.rideaustin.service.user;

import javax.annotation.Nonnull;

import org.apache.commons.mail.EmailException;

import com.google.common.collect.ImmutableMap;
import com.rideaustin.model.City;
import com.rideaustin.model.user.User;
import com.rideaustin.service.email.AbstractTemplateEmail;

public class UserCardIsUnlockedEmail extends AbstractTemplateEmail {

  private static final String TITLE = "Your credit card is unlocked";
  private static final String TEMPLATE = "user_card_unlocked.ftl";

  public UserCardIsUnlockedEmail(@Nonnull User user, @Nonnull City city) throws EmailException {
    super(TITLE, TEMPLATE);
    setModel(ImmutableMap.of("user", user, "city", city));
    addRecipient(user.getFullName(), user.getEmail());
    setFrom(city.getContactEmail(), city.getAppName());
  }

}
