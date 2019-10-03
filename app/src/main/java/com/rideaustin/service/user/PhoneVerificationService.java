package com.rideaustin.service.user;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rideaustin.model.PhoneVerificationItem;
import com.rideaustin.model.user.User;
import com.rideaustin.repo.dsl.PhoneVerificationItemDslRepository;
import com.rideaustin.rest.exception.BadRequestException;
import com.rideaustin.rest.exception.RideAustinException;
import com.rideaustin.rest.model.AuthenticationToken;
import com.rideaustin.service.AuthTokenUtils;
import com.rideaustin.service.CurrentUserService;
import com.rideaustin.service.generic.TimeService;
import com.rideaustin.service.thirdparty.CommunicationService;
import com.rideaustin.service.thirdparty.CommunicationServiceFactory;
import com.rideaustin.service.validation.phone.PhoneNumberCheckerService;
import com.rideaustin.utils.PhoneNumberUtils;
import com.rideaustin.utils.RandomString;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class PhoneVerificationService {

  private static final String CODE_CHARSET = "0123456789";
  private static final int CODE_LENGTH = 4;
  private static final String SMS_VERIFICATION_FAILED_MESSAGE = "SMS verification failed";

  private final PhoneVerificationItemDslRepository phoneVerificationItemDslRepository;
  private final CommunicationServiceFactory communicationServiceFactory;
  private final TimeService timeService;
  private final PhoneNumberCheckerService phoneNumberCheckerService;
  private final CurrentUserService currentUserService;

  public AuthenticationToken initiate(String phoneNumber) throws RideAustinException {
    validatePhoneNumber(phoneNumber);
    String formattedPhoneNumber = PhoneNumberUtils.preponeWithUSCountryCode(phoneNumber);

    PhoneVerificationItem verificationItem = new PhoneVerificationItem();
    verificationItem.setPhoneNumber(formattedPhoneNumber);
    verificationItem.setAuthToken(AuthTokenUtils.generateAuthToken());
    verificationItem.setVerificationCode(RandomString.generate(CODE_CHARSET, CODE_LENGTH));
    phoneVerificationItemDslRepository.save(verificationItem);
    AuthenticationToken token = new AuthenticationToken();
    token.setToken(verificationItem.getAuthToken());

    CommunicationService.SmsStatus status;
    try {
      status = communicationServiceFactory.createCommunicationService()
        .sendSms(new PhoneVerificationSMS(verificationItem));
    } catch (Exception e) {
      log.error(SMS_VERIFICATION_FAILED_MESSAGE, e);
      throw new BadRequestException(SMS_VERIFICATION_FAILED_MESSAGE);
    }
    if (status.equals(CommunicationService.SmsStatus.INVALID_PHONE_NUMBER)) {
      throw new BadRequestException(SMS_VERIFICATION_FAILED_MESSAGE);
    }

    return token;
  }

  private void validatePhoneNumber(String phoneNumber) throws RideAustinException {
    if (StringUtils.isEmpty(phoneNumber)) {
      throw new BadRequestException("Please enter a valid phone number");
    }
    if (phoneNumber.length() < 6) {
      throw new BadRequestException("Please enter a valid phone number");
    }

    User currentUser = currentUserService.getUser();
    if (currentUser == null || !currentUser.isDriver()) {
      phoneNumberCheckerService.check(phoneNumber);
    }
  }

  public Boolean verify(String authToken, String code) {
    PhoneVerificationItem verificationItem = phoneVerificationItemDslRepository.findVerificationItem(authToken, code);
    if (verificationItem == null) {
      return Boolean.FALSE;
    }
    if (verificationItem.getVerifiedOn() != null) {
      return Boolean.FALSE;
    }
    verificationItem.setVerifiedOn(timeService.getCurrentDate());
    phoneVerificationItemDslRepository.save(verificationItem);
    return Boolean.TRUE;
  }
}
