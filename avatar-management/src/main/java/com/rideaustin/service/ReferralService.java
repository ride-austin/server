package com.rideaustin.service;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.mail.EmailException;
import org.apache.commons.validator.routines.EmailValidator;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rideaustin.model.City;
import com.rideaustin.model.user.Driver;
import com.rideaustin.rest.exception.BadRequestException;
import com.rideaustin.rest.exception.RideAustinException;
import com.rideaustin.rest.exception.ServerError;
import com.rideaustin.service.email.EmailService;
import com.rideaustin.service.thirdparty.CommunicationService;
import com.rideaustin.service.thirdparty.CommunicationServiceFactory;
import com.rideaustin.service.user.ReferADriverEmail;
import com.rideaustin.service.user.ReferADriverSMS;
import com.rideaustin.utils.PhoneNumberUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class ReferralService {

  private final CommunicationServiceFactory communicationServiceFactory;
  private final DriverService driverService;
  private final CurrentUserService currentUserService;
  private final CityService cityService;
  private final EmailService emailService;

  private final EmailValidator emailValidator = EmailValidator.getInstance();

  private final Environment environment;


  public void referAFriendBySMS(Long id, String phoneNumber, Long cityId) throws RideAustinException {
    validatePhoneNumber(phoneNumber);
    String formattedPhoneNumber = PhoneNumberUtils.preponeWithUSCountryCode(phoneNumber);
    Driver driver = driverService.findDriver(id, currentUserService.getUser());
    City currentCity;
    if (cityId == null) {
      currentCity = cityService.getCityForCurrentClientAppVersionContext();
    } else {
      currentCity = cityService.getCityOrThrow(cityId);
    }

    CommunicationService.SmsStatus status = communicationServiceFactory.createCommunicationService()
      .sendSms(new ReferADriverSMS(driver, currentCity, formattedPhoneNumber));
    if (status == CommunicationService.SmsStatus.INVALID_PHONE_NUMBER) {
      throw new BadRequestException("Please enter a valid phone number");
    }
  }

  public void referAFriendByEmail(Long id, String refereeEmail, Long cityId) throws RideAustinException {
    validateEmail(refereeEmail);
    City currentCity;
    if (cityId == null) {
      currentCity = cityService.getCityForCurrentClientAppVersionContext();
    } else {
      currentCity = cityService.getCityOrThrow(cityId);
    }

    Driver driver = driverService.findDriver(id, currentUserService.getUser());
    try {
      emailService.sendEmail(new ReferADriverEmail(driver, currentCity, refereeEmail,
        environment.getProperty("sender.email", currentCity.getContactEmail()),
        currentCity.getAppName()));
    } catch (EmailException e) {
      throw new ServerError(e);
    }
  }

  private void validatePhoneNumber(String phoneNumber) throws BadRequestException {
    if (StringUtils.isEmpty(phoneNumber)) {
      throw new BadRequestException("Please enter a valid phone number");
    }
  }

  private void validateEmail(String refereeEmail) throws BadRequestException {
    if (StringUtils.isEmpty(refereeEmail)) {
      throw new BadRequestException("Please enter a valid email address");
    }
    if (!emailValidator.isValid(refereeEmail)) {
      throw new BadRequestException("Please enter a valid email address");
    }
  }
}
