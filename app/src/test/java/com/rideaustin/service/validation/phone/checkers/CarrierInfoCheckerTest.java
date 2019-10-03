package com.rideaustin.service.validation.phone.checkers;

import static com.rideaustin.service.thirdparty.lookup.PhoneNumberInfo.PhoneNumberStatus.EXISTENT;
import static com.rideaustin.service.thirdparty.lookup.PhoneNumberInfo.PhoneNumberStatus.NON_EXISTENT;
import static com.rideaustin.service.thirdparty.lookup.PhoneNumberInfo.PhoneNumberType.MOBILE;
import static com.rideaustin.service.thirdparty.lookup.PhoneNumberInfo.PhoneNumberType.UNKNOWN;
import static com.rideaustin.service.thirdparty.lookup.PhoneNumberInfo.PhoneNumberType.VOIP;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Optional;

import org.junit.Test;

import com.rideaustin.service.thirdparty.lookup.PhoneNumberInfo;

public class CarrierInfoCheckerTest {

  private PhoneNumberChecker carrierInfoChecker = new CarrierInfoChecker();

  private final String countryCode = "US";

  @Test
  public void shouldNotReturnError_WhenPhoneTypeIsMobile() {
    final String mobileNumber = "7872345678";
    final PhoneNumberInfo phoneNumberInfo = new PhoneNumberInfo(mobileNumber, countryCode, MOBILE, EXISTENT);

    Optional<PhoneNumberChecker.CheckerError> error = carrierInfoChecker.check(phoneNumberInfo);

    assertFalse(error.isPresent());
  }

  @Test
  public void shouldReturnError_WhenPhoneTypeIsVoip() {
    final String voipNumber = "6464702534";
    final PhoneNumberInfo phoneNumberInfo = new PhoneNumberInfo(voipNumber, countryCode, VOIP, EXISTENT);

    Optional<PhoneNumberChecker.CheckerError> error = carrierInfoChecker.check(phoneNumberInfo);

    assertTrue(error.isPresent());
  }

  @Test
  public void shouldNotReturnError_WhenPhoneTypeIsUnknown() {
    final String number = "1234445566";
    final PhoneNumberInfo phoneNumberInfo = new PhoneNumberInfo(number, countryCode, UNKNOWN, EXISTENT);

    Optional<PhoneNumberChecker.CheckerError> error = carrierInfoChecker.check(phoneNumberInfo);

    assertFalse(error.isPresent());
  }

  @Test
  public void shouldNotReturnError_WhenPhoneTypeIsNull() {
    final String number = "1234445566";
    final PhoneNumberInfo phoneNumberInfo = new PhoneNumberInfo(number, countryCode, null, NON_EXISTENT);

    Optional<PhoneNumberChecker.CheckerError> error = carrierInfoChecker.check(phoneNumberInfo);

    assertFalse(error.isPresent());
  }
}
