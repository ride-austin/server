package com.rideaustin.service.user;

import org.apache.commons.mail.EmailException;

import com.google.common.collect.ImmutableMap;
import com.rideaustin.Constants;
import com.rideaustin.model.City;
import com.rideaustin.model.user.Rider;
import com.rideaustin.service.email.AbstractTemplateEmail;

public class RiderSignUpEmail extends AbstractTemplateEmail {

  public static final String TITLE = Constants.EmailTitle.RIDER_SIGNUP_EMAIL;
  private static final String TEMPLATE = "rider_signup.ftl";

  public RiderSignUpEmail(Rider rider, City city) throws EmailException {
    super(TITLE, TEMPLATE);
    setModel(ImmutableMap.of("rider", rider, "city", city));
    addRecipient(rider.getFullName(), rider.getEmail());
    setFrom(city.getContactEmail(), city.getAppName());
  }

}
