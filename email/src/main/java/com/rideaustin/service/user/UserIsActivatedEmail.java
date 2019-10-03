package com.rideaustin.service.user;

import org.apache.commons.mail.EmailException;

import com.google.common.collect.ImmutableMap;
import com.rideaustin.Constants;
import com.rideaustin.model.City;
import com.rideaustin.service.email.AbstractTemplateEmail;

public class UserIsActivatedEmail extends AbstractTemplateEmail {

  public static final String TITLE = Constants.EmailTitle.USER_IS_ACTIVATED_EMAIL;
  private static final String TEMPLATE = "user_activated.ftl";

  public UserIsActivatedEmail(String email, String fullName, City city) throws EmailException {
    super(TITLE, TEMPLATE);
    setModel(ImmutableMap.of("fullName", fullName, "city", city));
    addRecipient(fullName, email);
    setFrom(city.getContactEmail(), city.getAppName());
  }

}
