package com.rideaustin.service.validation.phone;

import static com.rideaustin.Constants.ErrorMessages.PHONE_NUMBER_REQUIRED;
import static com.rideaustin.utils.FraudLogUtil.fraudLog;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import com.rideaustin.rest.exception.BadRequestException;
import com.rideaustin.rest.exception.RideAustinException;
import com.rideaustin.service.thirdparty.lookup.PhoneNumberInfo;
import com.rideaustin.service.thirdparty.lookup.PhoneNumberLookupService;
import com.rideaustin.service.validation.phone.checkers.PhoneNumberChecker;
import com.rideaustin.service.validation.phone.checkers.PhoneNumberChecker.CheckerError;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class DefaultPhoneNumberCheckerService implements PhoneNumberCheckerService {

  private final Collection<PhoneNumberChecker> checkers;

  private final PhoneNumberLookupService lookupService;

  @Override
  public void check(String phoneNumber) throws RideAustinException {
    if (StringUtils.isBlank(phoneNumber)) {
      throw new BadRequestException(PHONE_NUMBER_REQUIRED);
    }

    PhoneNumberInfo lookupInfo = lookupService.lookup(phoneNumber);
    for (PhoneNumberChecker checker : checkers) {
      Optional<CheckerError> error = checker.check(lookupInfo);
      if (error.isPresent()) {
        log.error("Error occurred when checking phone number, %s", error.get());
        fraudLog(log, String.format("Attempt to use voip number %s", lookupInfo.getRawNumber()));
        throw new BadRequestException(error.get().getMessage());
      }
    }
  }

  @Override
  public Collection<PhoneNumberChecker> getCheckers() {
    return Collections.unmodifiableCollection(checkers);
  }
}
