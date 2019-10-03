package com.rideaustin.service.thirdparty.lookup;

import com.rideaustin.rest.exception.ServerError;

@FunctionalInterface
public interface PhoneNumberLookupService {
  PhoneNumberInfo lookup(String phoneNumber) throws ServerError;
}
