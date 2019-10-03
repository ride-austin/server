package com.rideaustin.service.support;

import javax.inject.Inject;
import javax.mail.internet.AddressException;
import javax.validation.constraints.NotNull;

import org.apache.commons.mail.EmailException;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rideaustin.filter.ClientType;
import com.rideaustin.model.City;
import com.rideaustin.model.SupportTopic;
import com.rideaustin.model.enums.AvatarType;
import com.rideaustin.model.ride.Ride;
import com.rideaustin.model.user.Driver;
import com.rideaustin.model.user.Rider;
import com.rideaustin.model.user.User;
import com.rideaustin.rest.exception.NotFoundException;
import com.rideaustin.rest.exception.RideAustinException;
import com.rideaustin.rest.exception.ServerError;
import com.rideaustin.service.CityService;
import com.rideaustin.service.RideService;
import com.rideaustin.service.SupportTopicService;
import com.rideaustin.service.email.EmailService;
import com.rideaustin.service.user.DefaultSupportMessageEmail;
import com.rideaustin.service.user.SupportMessageEmail;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class SupportService {

  private final RideService rideService;
  private final EmailService emailService;
  private final CityService cityService;
  private final SupportTopicService supportTopicService;
  private final Environment environment;

  public void sendRideSupportEmail(User user, String message, Long rideId, Long cityId, ClientType clientType) throws RideAustinException {
    Ride ride = rideService.getRide(rideId);
    Driver driver = null;
    if (ride.getActiveDriver() != null) {
      driver = ride.getActiveDriver().getDriver();
    }
    sendSupportEmail(ride, driver, ride.getRider(), user, message, cityId, clientType);
  }

  public void sendGenericSupportEmail(User user, String message, Long cityId, ClientType clientType) throws RideAustinException {
    sendSupportEmail(null, null, null, user, message, cityId, clientType);
  }

  private void sendSupportEmail(Ride ride, Driver driver, Rider rider, User user, String message, @NotNull Long cityId, ClientType clientType) throws RideAustinException {
    try {
      City city = cityService.getById(cityId);
      emailService.sendEmail(new SupportMessageEmail(user, ride, rider, driver, message,
        city.getSupportEmail(), city.getAppName(), city.getSupportEmail(), city, clientType));
    } catch (EmailException | AddressException e) {
      throw new ServerError(e);
    }
  }

  public void sendDefaultSupportEmail(User user, AvatarType avatarType, Long rideId,
    Long topicId, String comments) throws RideAustinException {
    try {
      Ride ride = rideService.getRide(rideId);
      City city = cityService.getById(ride.getCityId());
      String managementConsoleUrl = environment.getProperty("management-console.url");
      SupportTopic topic = supportTopicService.getSupportTopic(topicId);
      SupportTopic subTopic = null;
      if (topic.getParent() != null) {
        subTopic = topic;
        topic = subTopic.getParent();
      }
      DefaultSupportMessageEmail supportMessageEmail
        = new DefaultSupportMessageEmail(user, avatarType, city, ride, managementConsoleUrl,
        topic, subTopic, comments);
      emailService.sendEmail(supportMessageEmail);
    } catch (NotFoundException | EmailException | AddressException e) {
      throw new ServerError(e);
    }

  }
}
