package com.rideaustin.service.user;

import static com.rideaustin.Constants.ErrorMessages.PHONE_NUMBER_NO_VOIP;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Date;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.common.collect.ImmutableSet;
import com.rideaustin.model.PhoneVerificationItem;
import com.rideaustin.model.enums.AvatarType;
import com.rideaustin.model.user.User;
import com.rideaustin.repo.dsl.PhoneVerificationItemDslRepository;
import com.rideaustin.rest.exception.BadRequestException;
import com.rideaustin.rest.exception.RideAustinException;
import com.rideaustin.rest.model.AuthenticationToken;
import com.rideaustin.service.AuthTokenUtils;
import com.rideaustin.service.CurrentUserService;
import com.rideaustin.service.generic.TimeService;
import com.rideaustin.service.thirdparty.AmazonSNSService;
import com.rideaustin.service.thirdparty.CommunicationService;
import com.rideaustin.service.thirdparty.CommunicationServiceFactory;
import com.rideaustin.service.validation.phone.PhoneNumberCheckerService;

@RunWith(MockitoJUnitRunner.class)
public class PhoneVerificationServiceTest {

  private static final String AUTH_TOKEN = "123123123";
  private static final String CODE = "1234";
  private static final String PHONE_NUMBER = "+1 555 555 555";
  @Rule
  public ExpectedException expectedException = ExpectedException.none();
  @Mock
  private PhoneVerificationItemDslRepository phoneVerificationItemDslRepository;
  @Mock
  private AuthTokenUtils authTokenService;
  @Mock
  private AmazonSNSService amazonSNSService;
  @Mock
  private CommunicationServiceFactory communicationServiceFactory;
  @Mock
  private TimeService timeService;
  @Mock
  private PhoneNumberCheckerService phoneNumberCheckerService;
  @Mock
  private CurrentUserService currentUserService;

  @InjectMocks
  private PhoneVerificationService phoneVerificationService;

  @Before
  public void setup() throws Exception {
    when(communicationServiceFactory.createCommunicationService()).thenReturn(amazonSNSService);
  }

  @Test
  public void testInitializePhoneVerificationInvalidPhone() throws RideAustinException {
    expectedException.expect(BadRequestException.class);
    expectedException.expectMessage("Please enter a valid phone number");

    phoneVerificationService.initiate(null);
  }

  @Test
  public void testInitializePhoneVerificationPhoneNumberToShort() throws RideAustinException {
    expectedException.expect(BadRequestException.class);
    expectedException.expectMessage("Please enter a valid phone number");

    phoneVerificationService.initiate("+1234");
  }

  @Test
  public void testInitializePhoneVerificationPhoneNumberIsVoipForRider() throws RideAustinException {
    expectedException.expect(BadRequestException.class);
    expectedException.expectMessage(PHONE_NUMBER_NO_VOIP);

    User rider = new User();
    rider.setAvatarTypes(ImmutableSet.of(AvatarType.RIDER));
    final String voipNumber = "+16464702534";
    doThrow(new BadRequestException(PHONE_NUMBER_NO_VOIP)).when(phoneNumberCheckerService).check(voipNumber);
    when(currentUserService.getUser()).thenReturn(rider);

    phoneVerificationService.initiate(voipNumber);
  }

  @Test
  public void testInitializePhoneVerification() throws RideAustinException {
    when(amazonSNSService.sendSms(any(PhoneVerificationSMS.class))).thenReturn(CommunicationService.SmsStatus.OK);

    AuthenticationToken token = phoneVerificationService.initiate(PHONE_NUMBER);

    ArgumentCaptor<PhoneVerificationItem> argument = ArgumentCaptor.forClass(PhoneVerificationItem.class);
    verify(phoneVerificationItemDslRepository, times(1)).save(argument.capture());

    verify(amazonSNSService, times(1)).sendSms(any(PhoneVerificationSMS.class));

    PhoneVerificationItem createdItem = argument.getValue();
    assertThat(token.getToken(), is(not(nullValue())));
    assertThat(createdItem.getAuthToken(), equalTo(token.getToken()));
    assertThat(createdItem.getVerificationCode(), is(not(nullValue())));
  }

  @Test
  public void testInitializePhoneVerificationInvalidPhoneNumber() throws RideAustinException {
    expectedException.expect(BadRequestException.class);
    expectedException.expectMessage("SMS verification failed");

    when(amazonSNSService.sendSms(any(PhoneVerificationSMS.class))).thenReturn(CommunicationService.SmsStatus.INVALID_PHONE_NUMBER);

    phoneVerificationService.initiate(PHONE_NUMBER);
  }

  @Test
  public void testVerifyPhone() throws RideAustinException {

    when(phoneVerificationItemDslRepository.findVerificationItem(anyString(), anyString()))
      .thenReturn(mockPhoneVerificationItem());

    Boolean result = phoneVerificationService.verify(AUTH_TOKEN, CODE);
    assertEquals(Boolean.TRUE, result);
  }

  @Test
  public void testVerifyPhoneInvalid() throws RideAustinException {
    when(phoneVerificationItemDslRepository.findVerificationItem(anyString(), anyString()))
      .thenReturn(null);

    Boolean result = phoneVerificationService.verify(AUTH_TOKEN, CODE);
    assertEquals(Boolean.FALSE, result);
  }

  @Test
  public void testVerifyPhoneAlreadyVerified() throws RideAustinException {
    PhoneVerificationItem item = mockPhoneVerificationItem();
    item.setVerifiedOn(new Date());
    when(phoneVerificationItemDslRepository.findVerificationItem(anyString(), anyString()))
      .thenReturn(item);

    Boolean result = phoneVerificationService.verify(AUTH_TOKEN, CODE);
    assertEquals(Boolean.FALSE, result);
  }

  private PhoneVerificationItem mockPhoneVerificationItem() {
    PhoneVerificationItem item = new PhoneVerificationItem();
    item.setAuthToken(AUTH_TOKEN);
    item.setVerificationCode(CODE);
    return item;
  }

}