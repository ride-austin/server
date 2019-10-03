package com.rideaustin.service.validation.phone.checkers;

import static com.rideaustin.Constants.ErrorMessages.PHONE_NUMBER_NO_VOIP;
import static com.rideaustin.service.thirdparty.lookup.PhoneNumberInfo.PhoneNumberType.VOIP;

import java.util.Optional;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.rideaustin.service.thirdparty.lookup.PhoneNumberInfo;

@Component
@Order(1)
public class CarrierInfoChecker implements PhoneNumberChecker {

  @Override
  public Optional<CheckerError> check(PhoneNumberInfo info) {
    if (info.getType() != null && info.getType() == VOIP) {
      return Optional.of(new CheckerError(PHONE_NUMBER_NO_VOIP));
    }

    return Optional.empty();
  }
}
