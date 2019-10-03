package com.rideaustin.service.thirdparty.lookup;

import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Service;

import com.rideaustin.service.thirdparty.lookup.condition.MockLookupServiceCondition;

@Service
@Conditional(MockLookupServiceCondition.class)
public class MockPhoneNumberLookupService implements PhoneNumberLookupService {

  private static final String DEFAULT_COUNTRY_CODE = "US";

  @Override
  public PhoneNumberInfo lookup(String phoneNumber) {
    return new PhoneNumberInfo(phoneNumber, DEFAULT_COUNTRY_CODE, PhoneNumberInfo.PhoneNumberType.MOBILE, PhoneNumberInfo.PhoneNumberStatus.EXISTENT);
  }
}
