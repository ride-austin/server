package com.rideaustin.service.payment;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.mail.EmailException;

import com.rideaustin.model.City;
import com.rideaustin.model.ride.Ride;
import com.rideaustin.model.user.RiderCard;
import com.rideaustin.service.email.AbstractTemplateEmail;

public class InvalidPaymentEmail extends AbstractTemplateEmail {

  public static final String TITLE = "Payment was declined";
  private static final String RIDER_TEMPLATE = "invalid_payment_rider.ftl";

  public InvalidPaymentEmail(Ride ride, RiderCard card, City city) throws EmailException {
    super(TITLE, RIDER_TEMPLATE);
    Map<String, Object> model = new HashMap<>();
    model.put("ride", ride);
    model.put("card", card);
    model.put("city", city);
    setModel(model);
    setFrom(city.getContactEmail(), city.getAppName());
  }
}
