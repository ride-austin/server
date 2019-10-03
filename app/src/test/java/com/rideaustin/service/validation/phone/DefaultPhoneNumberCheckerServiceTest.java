package com.rideaustin.service.validation.phone;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.google.common.collect.ImmutableSet;
import com.rideaustin.Constants;
import com.rideaustin.rest.exception.BadRequestException;
import com.rideaustin.rest.exception.RideAustinException;
import com.rideaustin.service.thirdparty.lookup.PhoneNumberInfo;
import com.rideaustin.service.thirdparty.lookup.PhoneNumberInfo.PhoneNumberStatus;
import com.rideaustin.service.thirdparty.lookup.PhoneNumberInfo.PhoneNumberType;
import com.rideaustin.service.thirdparty.lookup.PhoneNumberLookupService;
import com.rideaustin.service.validation.phone.checkers.PhoneNumberChecker;
import com.rideaustin.service.validation.phone.checkers.PhoneNumberChecker.CheckerError;

public class DefaultPhoneNumberCheckerServiceTest {

  @Mock
  private PhoneNumberChecker checker;
  @Mock
  private PhoneNumberLookupService lookupService;

  private DefaultPhoneNumberCheckerService testedInstance;

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);

    testedInstance = new DefaultPhoneNumberCheckerService(ImmutableSet.of(checker), lookupService);
  }

  @Test
  public void checkThrowsExceptionOnEmptyPhoneNumber() throws RideAustinException {
    expectedException.expect(BadRequestException.class);
    expectedException.expectMessage(Constants.ErrorMessages.PHONE_NUMBER_REQUIRED);

    testedInstance.check("");
  }

  @Test
  public void checkThrowsExceptionWhenLookupReturnsError() throws RideAustinException {
    final String errorMessage = "Error";
    final String phoneNumber = "+15125555555";
    when(lookupService.lookup(phoneNumber)).thenReturn(new PhoneNumberInfo("5125555555", "+1",
      PhoneNumberType.MOBILE, PhoneNumberStatus.EXISTENT));
    when(checker.check(any(PhoneNumberInfo.class))).thenReturn(Optional.of(new CheckerError(errorMessage)));

    expectedException.expect(BadRequestException.class);
    expectedException.expectMessage(errorMessage);

    testedInstance.check(phoneNumber);
  }

  @Test
  public void checkThrowsNoErrorWhenLookupReturnsNoError() throws RideAustinException {
    final String phoneNumber = "+15125555555";
    when(lookupService.lookup(phoneNumber)).thenReturn(new PhoneNumberInfo("5125555555", "+1",
      PhoneNumberType.MOBILE, PhoneNumberStatus.EXISTENT));
    when(checker.check(any(PhoneNumberInfo.class))).thenReturn(Optional.empty());

    testedInstance.check(phoneNumber);
  }
}