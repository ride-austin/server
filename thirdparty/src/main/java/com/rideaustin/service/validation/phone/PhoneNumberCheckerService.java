package com.rideaustin.service.validation.phone;

import java.util.Collection;

import com.rideaustin.rest.exception.RideAustinException;
import com.rideaustin.service.validation.phone.checkers.PhoneNumberChecker;

public interface PhoneNumberCheckerService {
  void check(String phoneNumber) throws RideAustinException;

  Collection<PhoneNumberChecker> getCheckers();
}
