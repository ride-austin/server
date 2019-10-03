package com.rideaustin.service.validation.phone;

import static com.rideaustin.Constants.ErrorMessages.PHONE_NUMBER_NO_VOIP;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collection;

import javax.inject.Inject;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.util.ReflectionTestUtils;

import com.rideaustin.config.AppConfig;
import com.rideaustin.rest.exception.BadRequestException;
import com.rideaustin.rest.exception.RideAustinException;
import com.rideaustin.service.thirdparty.lookup.PhoneNumberInfo;
import com.rideaustin.service.thirdparty.lookup.PhoneNumberLookupService;
import com.rideaustin.service.validation.phone.checkers.CarrierInfoChecker;
import com.rideaustin.service.validation.phone.checkers.PhoneNumberChecker;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {AppConfig.class})
@DirtiesContext
@WebAppConfiguration
public class DefaultPhoneNumberCheckerServiceIT {

  @Inject
  private PhoneNumberCheckerService checkerService;

  @Inject
  private CarrierInfoChecker carrierInfoChecker;

  @Mock
  private PhoneNumberLookupService lookupService;

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  private final String phoneNumber = "19995554433";

  private final String countryCode = "US";

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    ReflectionTestUtils.setField(checkerService, "lookupService", lookupService);
  }

  @Test
  public void shouldInitializeCheckers() {
    Collection<PhoneNumberChecker> checkers = checkerService.getCheckers();

    assertTrue(checkers != null);
    assertEquals(1, checkers.size());
    assertTrue(checkers.contains(carrierInfoChecker));
  }

  @Test
  public void shouldValidate_ForMobileNumber() throws RideAustinException {
    when(lookupService.lookup(any())).thenReturn(new PhoneNumberInfo(phoneNumber, countryCode, PhoneNumberInfo.PhoneNumberType.MOBILE, PhoneNumberInfo.PhoneNumberStatus.EXISTENT));

    try {
      checkerService.check(phoneNumber);
    } catch (BadRequestException e) {
      fail("Should have validated mobile phone");
    }

    verify(lookupService, times(1)).lookup(any());
  }

  @Test
  public void shouldNotValidate_ForVoipNumber() throws RideAustinException {
    thrown.expect(BadRequestException.class);
    thrown.expectMessage(PHONE_NUMBER_NO_VOIP);
    when(lookupService.lookup(any())).thenReturn(new PhoneNumberInfo(phoneNumber, countryCode, PhoneNumberInfo.PhoneNumberType.VOIP, PhoneNumberInfo.PhoneNumberStatus.EXISTENT));

    checkerService.check(phoneNumber);
  }
}
