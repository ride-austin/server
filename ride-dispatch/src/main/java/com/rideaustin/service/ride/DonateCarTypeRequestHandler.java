package com.rideaustin.service.ride;

import org.apache.commons.mail.EmailException;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.google.maps.model.LatLng;
import com.rideaustin.model.user.User;
import com.rideaustin.rest.exception.BadRequestException;
import com.rideaustin.rest.exception.RideAustinException;
import com.rideaustin.service.CityCache;
import com.rideaustin.service.email.EmailService;
import com.rideaustin.service.user.DonateEmail;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class DonateCarTypeRequestHandler implements CarTypeRequestHandler {

  private final EmailService emailService;
  private final CityCache cityCache;
  private final String recipient;

  public DonateCarTypeRequestHandler(EmailService emailService, CityCache cityCache, Environment environment) {
    this.emailService = emailService;
    this.cityCache = cityCache;
    this.recipient = environment.getProperty("donate.email.recipient", "rideforwhatmatters@example.com");
  }

  @Override
  public void handleRequest(User rider, String address, LatLng location, String comment, Long cityId) throws RideAustinException {
    try {
      emailService.sendEmail(new DonateEmail(recipient, rider.getFullName(), rider.getEmail(),
        address, location.lat, location.lng, comment, cityCache.getCity(cityId)));
    } catch (EmailException e) {
      log.error("Failed to send donate notification", e);
    }
    throw new BadRequestException("Thank you for your donation. This is your confirmation that we received your pickup request (disregard the Oops title). Please leave your donation/package at your front door and a volunteer will pickup your items between 11am and 5pm. Thank you for your support. #HappyHolidays #RideForWhatMatters");
  }
}
